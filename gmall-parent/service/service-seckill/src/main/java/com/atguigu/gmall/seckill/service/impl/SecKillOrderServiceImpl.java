package com.atguigu.gmall.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.mapper.SecKillOrderMapper;
import com.atguigu.gmall.seckill.pojo.SeckillOrder;
import com.atguigu.gmall.seckill.pojo.UserRecode;
import com.atguigu.gmall.seckill.service.SecKillOrderService;
import com.atguigu.gmall.seckill.utils.DateUtil;
import com.atguigu.gmall.seckill.utils.ThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀商品订单的业务层
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SecKillOrderServiceImpl implements SecKillOrderService {

    @Resource
    private SecKillOrderMapper secKillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 新增秒杀订单，为了缓解下单的压力。秒杀使用MQ进行异步下单，实现伪下单真排队
     *
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    @Override
    public UserRecode addSecKillOrder(String time, String goodsId, String num) {
        int buyNum;
        //参数校验
        try {
            if (StringUtils.isEmpty(time) ||
                    StringUtils.isEmpty(goodsId) ||
                    StringUtils.isEmpty(num)) {
                throw new RuntimeException();
            }
            buyNum = Integer.parseInt(num);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("参数错误");
        }

        String username = ThreadLocalUtil.get();
        UserRecode userRecode = new UserRecode();

        //保证一个用户不会重复排队
        Long incr = redisTemplate.opsForValue().increment("User_Queue_Count_" + username, 1);
        //设置过期时间，和秒杀订单的有效期保持一致，防止用户一直不能提交订单。五分钟
        //除此之外，这个key在用户付款后，用户主动取消后删除掉
        redisTemplate.expire("User_Queue_Count_" + username, 300, TimeUnit.SECONDS);

        if (incr > 1) {
            //说明队列中已经有了一个，这个时候直接返回不能重复排队
            userRecode.setStatus(3);
            userRecode.setMsg("下单失败，不能重复排队");
            return userRecode;
        }

        //排队UserRecode
        userRecode.setUsername(username);
        userRecode.setCreateTime(new Date());
        userRecode.setStatus(1);
        userRecode.setGoodsId(goodsId);
        userRecode.setTime(time);
        userRecode.setNum(buyNum);
        userRecode.setMsg("排队中");

        //优化：使用异步编排，将写入redis和发送消息队列的任务异步化。但是这里不需要异步执行的结果，出现异常直接修改UserRecode的状态
        CompletableFuture.runAsync(() -> {
            //排队信息写入redis提供给用户查询
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);

            //写入消息队列，准备异步下单
            rabbitTemplate.convertAndSend("seckill_order_exchange",
                    "seckill.order.add",
                    JSONObject.toJSONString(userRecode));
        }, threadPoolExecutor).whenCompleteAsync((result, ex) -> {
            if (ex != null) {
                //异常时触发，修改UserRecode状态。重新写入到redis并返回
                userRecode.setStatus(3);
                userRecode.setMsg("秒杀失败，请重试");
                redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
            }
        });

        //结束
        return userRecode;
    }

    /**
     * 查询redis中排队实体，通过token中的username
     *
     * @return
     */
    @Override
    public UserRecode getUserRecode() {
        String username = ThreadLocalUtil.get();
        UserRecode userRecode = (UserRecode) redisTemplate.opsForValue().get("User_Recode_" + username);
        return userRecode;
    }

    /**
     * 真正下单的接口
     */
    @Override
    public void realSeckillOrderAdd(UserRecode userRecode) {
        String goodsId = userRecode.getGoodsId();
        String time = userRecode.getTime();
        String username = userRecode.getUsername();
        Integer num = userRecode.getNum();

        //判断商品是否存在
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash().get(time, goodsId);
        if (seckillGoods == null || seckillGoods.getId() == null) {
            //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
            throw new RuntimeException("秒杀商品不存在");
        }

        //判断商品是否在秒杀时间内
        String nowTime = DateUtil.data2str(DateUtil.getDateMenus().get(0), DateUtil.PATTERN_YYYYMMDDHH);
        if (!nowTime.equals(time)) {
            //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
            throw new RuntimeException("秒杀商品不在时间段内");
        }

        //判断购买的数量是否符合限购数量
        if (num <= 0 || num > seckillGoods.getSeckillLimit()) {
            //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
            throw new RuntimeException("秒杀商品超过限购数量");
        }

        //判断库存是否充足
        if (num > seckillGoods.getStockCount()) {
            //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
            throw new RuntimeException("秒杀商品库存不足");
        }

        //循环扣减库存（redis里的），从商品的list中获取n个元素，从右边出队
        for (int i = 0; i < num; i++) {
            Object o = redisTemplate.opsForList().rightPop("Seckill_Goods_Stock_Queue_" + goodsId);
            if (o == null) {
                //回滚库存，回滚 i 个，写回到list里
                String[] ids = getIds(i, goodsId);
                redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + goodsId, ids);

                //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
                throw new RuntimeException("秒杀商品库存不足");
            }
        }

        //对商品库存减n，由于上面减库存的保证，这里自增不会出现负数。并且这里的值是准确的库存值
        Long increment =
                redisTemplate.opsForHash()
                        .increment("SeckillGoodsStockCount_" + time, goodsId, -num);

        //从list出队一个元素后，如果下面的代码出现了异常，需要将元素重新从左边入队
        try {
            //异步将订单信息保存，写入MySQL
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(UUID.randomUUID().toString().replace("-", ""));
            seckillOrder.setGoodsId(goodsId);
            seckillOrder.setNum(num);
            seckillOrder.setMoney(seckillGoods.getCostPrice().multiply(new BigDecimal(num + "")).toString());
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(userRecode.getCreateTime());
            //TODO -- 支付时间
            seckillOrder.setStatus("0");
            CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(() -> {
                secKillOrderMapper.insert(seckillOrder);
                return true;
            }, threadPoolExecutor).exceptionally(ex -> {
                return false;
            });

            //将订单信息异步保存到redis中，提供给用户查询
            CompletableFuture<Boolean> future2 = CompletableFuture.supplyAsync(() -> {
                redisTemplate.opsForHash().put("User_Seckill_Order_" + time, seckillOrder.getId(), seckillOrder);
                return true;
            }, threadPoolExecutor).exceptionally(ex -> {
                return false;
            });

            //redis和MySQL有一个成功就算成功。如果两个都失败则下单失败
            if (!future1.get() && !future2.get()) {
                //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
                throw new RuntimeException("秒杀商品下单失败");
            }

            //写入到redis
            userRecode.setStatus(2);
            userRecode.setMoney(seckillOrder.getMoney());
            userRecode.setOrderId(seckillOrder.getId());
            userRecode.setMsg("秒杀下单成功，等待支付");
            redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);

            //秒杀下单的超时取消：使用延迟队列，和普通订单一样
            rabbitTemplate.convertAndSend("seckill_order_normal_exchange",
                    "seckill.order.normal",
                    username,
                    (message -> {
                        MessageProperties messageProperties = message.getMessageProperties();
                        messageProperties.setExpiration("300000");
                        return message;
                    }));

            //TODO -- 用户付款后应该和普通订单分开，在支付微服务单独创建一个提供给秒杀。支付服务解耦改造

        } catch (Exception e) {
            //有异常，需要将元素重新放回list回滚
            redisTemplate.opsForList().leftPush("Seckill_Goods_Stock_Queue_" + goodsId, goodsId);
            //回滚库存
            redisTemplate.opsForHash()
                    .increment("SeckillGoodsStockCount_" + time, goodsId, num);

            //直接抛运行时异常，由MQ的消费者统一进行修改排队状态，删除标识位
            throw new RuntimeException("秒杀下单商品失败");
        }
    }

    /**
     * 根据订单id取消订单
     *
     * @param username
     */
    @Override
    public void cancelSeckillOrder(String username) {
        //获取用户名，但是主动取消可以获取到，MQ取消获取不到 --> 那么username从哪里来？
        String status = "";
        if(username != null){
            //消息队列来的
            status = "3";
        }else {
            //用户主动取消
            status = "2";
            username = ThreadLocalUtil.get();
        }

        //查询redis里用户的排队状态
        UserRecode userRecode =
                (UserRecode) redisTemplate.opsForValue().get("User_Recode_" + username);

        //查询订单，根据orderId和订单状态
        SeckillOrder seckillOrder = secKillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getUserId, username)
                        .eq(SeckillOrder::getStatus, "0"));

        //判断订单是否存在
        if (seckillOrder == null || StringUtils.isEmpty(seckillOrder.getId())) {
            //说明数据库中不存在，再去redis中找
            seckillOrder = (SeckillOrder) redisTemplate.opsForHash().get("User_Seckill_Order_" + userRecode.getTime(), userRecode.getOrderId());
            if (seckillOrder == null || StringUtils.isEmpty(seckillOrder.getId())) {
                //说明redis里也没有，那就直接返回
                return;
            }
        }

        //能走到这里，seckillOrder一定不为空
        //更新订单状态
        seckillOrder.setStatus(status);
        int update = secKillOrderMapper.updateById(seckillOrder);
        if (update < 0) {
            throw new RuntimeException("取消订单失败");
        }

        rollbackGoodsStock(userRecode);

        //删除用来保证不会重复提交的key
        redisTemplate.delete("User_Queue_Count_" + username);
        //删除用户排队状态
        redisTemplate.delete("User_Recode_" + username);
        //删除redis里订单
        redisTemplate.opsForHash().delete("User_Seckill_Order_" + userRecode.getTime(), userRecode.getOrderId());

    }

    /**
     * 修改订单的状态
     *
     * @param result
     */
    @Override
    public void updateSeckillOrder(String result) {
        //反序列化
        Map<String, String> resultMap = JSONObject.parseObject(result, Map.class);
        String attachString = resultMap.get("attach");
        Map<String, String> attachMap = JSONObject.parseObject(attachString, Map.class);
        String username = attachMap.get("username");
        //从数据库查询订单
        SeckillOrder seckillOrder = secKillOrderMapper.selectOne(
                new LambdaQueryWrapper<SeckillOrder>()
                        .eq(SeckillOrder::getUserId, username)
                        .eq(SeckillOrder::getStatus, "0"));

        //查询用户排队状态
        UserRecode userRecode =
                (UserRecode) redisTemplate.opsForValue().get("User_Recode_" + username);

        //判断是否存在，不存在再去redis查
        if(seckillOrder == null || StringUtils.isEmpty(seckillOrder.getId())){
            seckillOrder =
                    (SeckillOrder) redisTemplate.opsForHash().get("User_Seckill_Order_" + userRecode.getTime(),
                            userRecode.getOrderId());
            if(seckillOrder == null || StringUtils.isEmpty(seckillOrder.getId())){
                return;
            }
        }

        //能执行到这里说明seckillOrder存在

        //修改数据库
        seckillOrder.setStatus("1");
        if("0".equals(resultMap.get("payWay"))){
            //说明是微信支付
            seckillOrder.setOutTradeNo(resultMap.get("transaction_id"));
        }else if("1".equals(resultMap.get("payWay"))){
            //说明是支付宝
            seckillOrder.setOutTradeNo("trade_no");
        }
        int update = secKillOrderMapper.updateById(seckillOrder);
        if(update <= 0){
            throw new RuntimeException("修改订单支付结果失败");
        }

        //删除标识位
        redisTemplate.delete("User_Queue_Count_" + username);
        //排队状态
        redisTemplate.delete("User_Recode_" + username);
        //删除redis中的临时订单数据
        redisTemplate.opsForHash().delete("User_Seckill_Order_" + userRecode.getTime(), username);
    }

    /**
     * 根据订单id回滚商品库存
     *
     * @param userRecode
     */
    private void rollbackGoodsStock(UserRecode userRecode) {
        //商品已经结束了秒杀，只用修改SeckillGoodsStockCount_TIME里的库存，因为其他数据已经清理掉了
        //自增库存
        Long increment = redisTemplate.opsForHash().increment("SeckillGoodsStockCount_" + userRecode.getTime(),
                userRecode.getGoodsId() + "",
                userRecode.getNum());

        //商品还在秒杀活动，还要修改TIME的hash，商品的队列
        SeckillGoods seckillGoods =
                (SeckillGoods) redisTemplate.opsForHash().get(userRecode.getTime(), userRecode.getGoodsId() + "");
        if (seckillGoods != null) {
            //说明商品还在秒杀
            //修改商品hash列表
            seckillGoods.setStockCount(increment.intValue());
            redisTemplate.opsForHash().put(userRecode.getTime(), userRecode.getGoodsId() + "", seckillGoods);
            //修改商品队列
            String[] ids = getIds(userRecode.getNum(), userRecode.getGoodsId());
            redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_" + userRecode.getGoodsId() + "", ids);
        }
    }

    /**
     * 自动生成商品list列表。这里使用数组使用是因为数组可以随机存取效率更高
     *
     * @param count
     * @param goodsId
     * @return
     */
    private String[] getIds(int count, String goodsId) {
        String[] ids = new String[count];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = goodsId + "";
        }
        return ids;
    }
}
