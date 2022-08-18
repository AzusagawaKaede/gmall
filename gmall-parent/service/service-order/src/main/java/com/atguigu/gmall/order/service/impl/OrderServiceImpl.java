package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.atguigu.gmall.cart.client.CartFeignService;
import com.atguigu.gmall.model.base.BaseEntity;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.utils.ThreadLocalUtil;
import com.atguigu.gmall.product.client.ProductFeignService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: 订单模块的业务层
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AlipayClient alipayClient;

    /**
     * 新增订单
     * 订单重复提交的三种情况：
     * 1 手速快，迅速点了多次提交订单
     * 2 浏览器回退，再次点击提交订单
     * 3 多端提交，手机端和电脑端一起提交
     * <p>
     * 解决订单重复提交的问题：redis实现分布式锁
     * 基于乐观锁的思想，当用户提交了第一次订单以后，立刻在redis自增一个键值对 incr
     * 只有当第一次时键值为1，只要获得的值不为1说明已经提交过
     * 在第一次提交订单结束后，清除redis的键
     *
     * @param orderInfo
     */
    @Override
    public void addOrder(OrderInfo orderInfo) {
        //参数校验
        if (orderInfo == null) {
            throw new RuntimeException("参数错误，新增订单失败");
        }

        /**
         * 再次查询用户选中状态下的购物项列表，总件数，总金额 -- 调用Cart微服务
         * BUG - 1：Feign发送的请求里没有token
         */
        Map map = cartFeignService.addOrder();
        //没有购物项选中
        if (map != null && map.isEmpty()) {
            throw new RuntimeException("购物车中没有选中的商品，新增订单失败");
        }

        //redis分布式锁保证订单不会重复提交，第一次提交时，key不存在，自增的结果一定是1。非1代表重复提交
        String username = ThreadLocalUtil.get();
        Long flag =
                redisTemplate.opsForValue().increment("generate_order_for_user:" + username);
        //为了防止程序故障无法执行到finally释放锁，我们需要设置key的存活时间
        redisTemplate.expire("generate_order_for_user:" + username, 10, TimeUnit.SECONDS);
        if (flag == 1) {
            try {
                //开始新增订单，保存OrderInfo
                orderInfo.setTotalAmount(new BigDecimal(map.get("totalMoney").toString()));
                orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
                orderInfo.setUserId("rlk");
                orderInfo.setCreateTime(new Date());
                orderInfo.setExpireTime(new Date(System.currentTimeMillis() + 1800000));
                orderInfo.setProcessStatus(OrderStatus.UNPAID.getComment());
                int insert = orderInfoMapper.insert(orderInfo);
                if (insert <= 0) {
                    throw new RuntimeException("新增订单失败");
                }

                //保存OrderDetail
                List cartInfoList = (List) map.get("cartInfoList");
                /**
                 * 获得每个商品skuId及数量的Map集合，用于减少库存
                 * 注意：尽管我们知道skuId和skuNum是Long和Integer类型，但是我们仍然要使用String或Object
                 * 因为Feign发起的http请求，实质上都只是传递字符串参数
                 */
                ConcurrentHashMap<String, Object> decreaseStockMap =
                        saveOrderDetails(orderInfo.getId(), cartInfoList);

                //减少商品库存
                Boolean stockFlag =
                        productFeignService.decreaseStock(decreaseStockMap);
                if (!stockFlag) {
                    throw new RuntimeException("扣减库存异常，新增订单失败");
                }

                //清空购物车已生成订单的购物项
                Boolean deleteFlag = cartFeignService.deleteCheck();
                if (!deleteFlag) {
                    throw new RuntimeException("删除购物车已下单项失败，新增订单失败");
                }

                //将订单号放到延迟队列中，实现超时取消 -- 超时时间设置为了队列统一的超时时间，这里不需要设置单条超时时间了
                rabbitTemplate.convertAndSend("order_normal_exchange",
                        "normal",
                        orderInfo.getId() + "");

            } catch (Exception e) {
                throw new RuntimeException("允许提交订单，但是提交订单时出现了异常：" + e.getMessage());
            } finally {
                //删除掉锁
                redisTemplate.delete("generate_order_for_user:" + username);
            }
        }


    }

    /**
     * 保存OrderDetail
     *
     * @param id
     * @param cartInfoList
     * @return
     */
    private ConcurrentHashMap<String, Object> saveOrderDetails(Long id, List cartInfoList) {

        /**
         * BUG - 2：ClassCastException 类型转换异常
         *  Java的强制类型转换是一种懒加载机制，只有被真正使用到时才会真正的去强制类型转换
         *  因此：尽管这里的cartInfoList是在addOrder()方法里强制类型转换，但是抛出异常的却是这里
         *
         *  在传输时，Java会将实体类序列化为Map，因此这里的cartInfo其实是一个Map，Map不能直接强转为CartInfo
         *  需要我们手动序列化，反序列化为CartInfo
         */

        /**
         * 为了方便后续做库存操作，这里遍历的时候我们同时保存每个商品购买的数量，返回一个集合
         * 流式处理线程不安全，需要使用ConcurrentHashMap
         */
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        cartInfoList.stream().forEach(o -> {
            String json = JSON.toJSONString(o);
            CartInfo cartInfo = JSONObject.parseObject(json, CartInfo.class);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(id);
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            int insert = orderDetailMapper.insert(orderDetail);
            if (insert <= 0) {
                throw new RuntimeException("新增订单失败");
            }
            map.put(cartInfo.getSkuId() + "", cartInfo.getSkuNum());
        });

        return map;
    }


    /**
     * 取消订单：在取消订单时，先向第三方支付平台取消订单
     *
     * @param orderId
     */
    @Override
    public void cancelOrder(Long orderId) {
        //参数校验
        if (orderId == null) {
            throw new RuntimeException("参数错误，取消订单失败");
        }

        //获取token
        String username = ThreadLocalUtil.get();

        //根据orderId和订单状态查询未支付的订单非空判断 -- 如果有token还需要判断userId
        //这里只查询未支付的订单，避免取消多次，保证幂等性
        OrderInfo orderInfo;
        if (username != null) {
            //用户取消：判断orderId，userId，orderStatus
            orderInfo = orderInfoMapper.selectOne(
                    new LambdaQueryWrapper<OrderInfo>()
                            .eq(OrderInfo::getId, orderId)
                            .eq(OrderInfo::getUserId, username)
                            .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.getComment()));
        } else {
            //超时取消：判断orderId，orderStatus
            orderInfo = orderInfoMapper.selectOne(
                    new LambdaQueryWrapper<OrderInfo>()
                            .eq(OrderInfo::getId, orderId)
                            .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.getComment()));
        }

        if (orderInfo == null || orderInfo.getId() == null) {
            //订单不存在
            return;
        }

        //向第三方支付平台发起取消清单请求 -- 必须是同步调用
//        cancelOrderPay(orderInfo);    //提示：4006 ISV权限不足，建议在开发者中心检查签约是否已经生效

        //修改订单状态，判断是用户取消订单还是超时自动取消 -- 依据：是否有token
        if (username != null) {
            //用户取消订单
            orderInfo.setOrderStatus(OrderStatus.USER_CANCEL.getComment());
            orderInfo.setProcessStatus(ProcessStatus.USER_CANCEL.getComment());
        } else {
            //超时取消订单
            orderInfo.setOrderStatus(OrderStatus.TIMEOUT_CANCEL.getComment());
            orderInfo.setProcessStatus(ProcessStatus.TIMEOUT_CANCEL.getComment());
        }

        //更新数据库
        int update = orderInfoMapper.updateById(orderInfo);
        if (update <= 0) {
            throw new RuntimeException("取消订单失败");
        }

        rollbackStock(orderId);

    }

    /**
     * 修改订单状态，接收JSON字符串
     *
     * @param result
     */
    @Override
    public void updateOrder(String result) {
        //解析JSON，获取订单号
        Map<String, String> map = JSONObject.parseObject(result, Map.class);
        //这里微信和支付宝的字段名是一致的，不需要再分别获取了
        String orderId = map.get("out_trade_no");

        /*
         * 根据订单号修改订单状态
         * 注意：虽然我们获取到了orderId但是我们不能直接使用orderId获取订单
         * 因为这里存在幂等性问题，可能由于网络问题第三方接口会多次请求回调接口导致消息队列有多条一样orderId的消息
         * 为了防止重复消费，除了根据orderId，我们还需要根据订单的当前状态来获取订单，即获取id是orderId且状态是未支付的订单
         */
        OrderInfo orderInfo = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getId, orderId)
                        .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.getComment()));

        if (orderInfo == null || orderInfo.getId() == null) {
            throw new RuntimeException("订单不存在或订单已超时取消");
        }

        //保存第三方支付的订单号（不是我们自己的订单号）
        orderInfo.setOrderStatus(OrderStatus.PAID.getComment());
        orderInfo.setProcessStatus(ProcessStatus.PAID.getComment());
        orderInfo.setTradeBody(result);
        if (map.get("payWay").equals("0")) {
            //说明是微信
            orderInfo.setOutTradeNo(map.get("transaction_id"));
            orderInfo.setPaymentWay("微信支付");
        } else {
            //说明是支付宝
            orderInfo.setOutTradeNo(map.get("trade_no"));
            orderInfo.setPaymentWay("支付宝支付");
        }

        //更新数据库
        int update = orderInfoMapper.updateById(orderInfo);
        if (update <= 0) {
            throw new RuntimeException("修改订单状态失败");
        }
    }

    /**
     * 回滚库存
     *
     * @param orderId
     * @return
     */
    private Boolean rollbackStock(Long orderId) {
        //查询该订单下的order_detail，构建map，key为skuId，value为skuNum
        List<OrderDetail> orderDetailList =
                orderDetailMapper.selectList(
                        new LambdaQueryWrapper<OrderDetail>()
                                .eq(OrderDetail::getOrderId, orderId));
        //流式处理线程不安全，使用ConcurrentHashMap
        Map<String, Object> skuParam = new ConcurrentHashMap<>();
        orderDetailList.stream().forEach(orderDetail -> {
            skuParam.put(orderDetail.getSkuId() + "", orderDetail.getSkuNum());
        });

        //远程调用product微服务回滚库存
        productFeignService.rollbackStock(skuParam);

        return true;
    }

    /**
     * 发起请求取消第三方订单支付
     *
     * @param orderInfo
     */
    private void cancelOrderPay(OrderInfo orderInfo) {
        if (orderInfo.getPaymentWay() == null) {
            return;
        }
        if (orderInfo.getPaymentWay().equals("支付宝支付")) {
            //向支付宝发起取消订单请求
            AlipayTradeCancelRequest request = new AlipayTradeCancelRequest();
            JSONObject bizContent = new JSONObject();
            bizContent.put("out_trade_no", orderInfo.getId());
            request.setBizContent(bizContent.toString());
            try {
                AlipayTradeCancelResponse response = alipayClient.execute(request);
                String result = response.getBody();
                Map resultMap = JSONObject.parseObject(result, Map.class);
                if(resultMap.get("action") == null){
                    throw new RuntimeException("支付宝取消订单支付失败");
                }
            } catch (AlipayApiException e) {
                e.printStackTrace();
                throw new RuntimeException("支付宝取消订单支付失败");
            }

        } else if (orderInfo.getPaymentWay().equals("微信支付")) {
            /**
             * 向微信发起取消订单请求，但是微信取消订单必须要求
             * 订单生成后不能马上调用关单接口，最短调用时间间隔为5分钟。
             * 因此对于微信支付的商品应该 改为退款
             */

        }
    }
}
