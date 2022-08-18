package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.utils.ThreadLocalUtil;
import com.atguigu.gmall.common.constant.CartConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.product.client.ProductFeignService;
import com.atguigu.gmall.user.client.UserFeignService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.AtomicDouble;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author: rlk
 * @date: 2022/8/9
 * Description: 购物车功能的业务层实现类
 * 使用购物车功能必须先登录，因此在购物车功能里，所有的操作必须要验证token里的username
 * 防止当前用户操作非当前用户的数据！！
 * 涉及到数据库的增删改，需要添加事务
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CartInfoServiceImpl implements CartInfoService {

    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private CartInfoMapper cartInfoMapper;
    @Resource
    private UserFeignService userFeignService;

    /**
     * 添加商品到购物车 -- 修改购物车商品的数量
     *
     * @param skuId
     * @param skuNum
     */
    @Override
    public void addCartInfo(Long skuId, Integer skuNum) {
        //参数校验，username不需要再校验，没有登录不会放行进来
        if (skuId == null || skuNum == null) {
            throw new RuntimeException("参数错误");
        }

        //根据skuId查询商品，判断是否存在 -- product-service远程调用
        SkuInfo skuInfo = productFeignService.getSkuInfoBySkuId(skuId);
        //判断是否存在
        if (skuInfo == null || skuInfo.getId() == null) {
            //不存在直接返回
            throw new RuntimeException("商品不存在");
        }

        //从ThreadLocal中获取username
        String username = ThreadLocalUtil.get();

        //先查询一次，判断该cartInfo记录是否存在，不存在新增，存在则是update
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getSkuId, skuId));

        if (cartInfo == null) {
            //说明是新增，保存到cart_info表
            //新增的情况下，判断skuNum是否大于0，不大于0直接返回
            if (skuNum <= 0) {
                return;
            }

            //商品存在，且skuNum大于0
            cartInfo = new CartInfo();
            cartInfo.setUserId(username);
            cartInfo.setSkuId(skuId);
            //查询商品价格
            BigDecimal price = productFeignService.getPriceBySkuId(skuId);
            cartInfo.setCartPrice(price);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            //保存到cart_info
            int insert = cartInfoMapper.insert(cartInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增商品失败");
            }
        } else {
            //说明是修改
            Integer oldSkuNum = cartInfo.getSkuNum();
            skuNum += oldSkuNum;
            //判断skuNum是否小于等于0，是则删除此行记录，不是则更新
            if (skuNum <= 0) {
                int delete = cartInfoMapper.deleteById(cartInfo.getId());
                if (delete <= 0) {
                    throw new RuntimeException("新增商品失败");
                }
            } else {
                cartInfo.setSkuNum(skuNum);
                int update = cartInfoMapper.updateById(cartInfo);
                if (update < 0) {
                    throw new RuntimeException("新增商品失败");
                }
            }
        }
    }

    /**
     * 购物车删除购物项，根据id删除但是还要判断username是否和id对应的userId一致
     *
     * @param cartInfoId
     */
    @Override
    public void deleteCartInfo(Long cartInfoId) {
        //参数校验
        if (cartInfoId == null) {
            throw new RuntimeException("参数错误");
        }

        //查询cartInfo
        CartInfo cartInfo = cartInfoMapper.selectById(cartInfoId);
        //判断是否为空，防止出现空指针
        if (cartInfo == null || cartInfo.getId() == null) {
            //说明记录不存在，直接返回
            return;
        }

        //判断cartInfo的userId是否和username相等，不相等说明不是id对应的用户，不允许删除
        if (!ThreadLocalUtil.get().equals(cartInfo.getUserId())) {
            return;
        }
        //相等，允许删除
        int delete = cartInfoMapper.deleteById(cartInfoId);
        if (delete <= 0) {
            throw new RuntimeException("删除失败");
        }
    }

    /**
     * 根据userId获取购物车列表
     *
     * @return
     */
    @Override
    public List<CartInfo> getCartInfoList() {
        return cartInfoMapper.selectList(new LambdaQueryWrapper<CartInfo>().eq(CartInfo::getUserId, ThreadLocalUtil.get()));
    }

    /**
     * 修改username下一个或所有的cartInfo的选中状态
     *
     * @param cartInfoId
     * @param status
     */
    @Override
    public void checkedOrUnchecked(Long cartInfoId, Short status) {
        //从ThreadLocal中获取username
        String username = ThreadLocalUtil.get();

        int update;
        //判断cartInfoId，为null表示修改当前username下所有的
        if (cartInfoId == null) {
            update = cartInfoMapper.updateAll(username, status);
        } else {
            //不为null，表示修改单个
            update = cartInfoMapper.updateOne(username, cartInfoId, status);
        }
    }

    /**
     * 查询用户本次生成订单的相关购物项信息，数据都使用token从数据库中查询
     *
     * @return
     */
    @Override
    public Map<String, Object> getOrderConfirm() {
        //获取username，也就是userId
        String username = ThreadLocalUtil.get();
        //准备一个Map存放返回数据
        Map<String, Object> result = new HashMap<>();

        /**
         * 查询收货地址 -- 远程调用service-user
         * BUG - 1：401-未登录
         *         这里使用的是Feign进行远程调用，Feign的底层实际上就是RestTemplate
         *         而RestTemplate底层还是发送的Http请求
         *         但是注意：Feign向远程服务发起的Http请求和用户发送给本服务的请求是两个Request
         *         因此：Feign发起的Http请求里没有token！
         * 解决：添加MVC的组件 - 拦截器，拦截Feign请求，在request里添加token相关的header！
         */
        List<UserAddress> userAddressList = userFeignService.getUserAddressListByUserId();
        result.put("userAddressList", userAddressList);

        //查询选中状态下的购物项列表
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, username)
                        .eq(CartInfo::getIsChecked, CartConst.CART_CHECKED));

        //当有选中的购物项时才进行计算
        if (!cartInfoList.isEmpty()) {
            /**
             * 这里不能使用普通的包装类Integer和Double，因为流式处理是并行的，线程不安全
             * 因此要使用线程安全的原子类，AtomicInteger和AtomicDouble
             */
            //总件数
            AtomicInteger totalCount = new AtomicInteger(0);
            //总金额
            AtomicDouble totalMoney = new AtomicDouble(0);
            //重新查询每个商品的价格，并计算总件数和总金额
            List<CartInfo> cartInfoListNew = cartInfoList.stream().map(cartInfo -> {
                //查询每个商品的价格，保存到cartInfo.skuPrice -- 调用productFeign
                BigDecimal price = productFeignService.getPriceBySkuId(cartInfo.getSkuId());
                cartInfo.setSkuPrice(price);

                /**
                 * 商品总数累加：addAndGet() - 先添加后获取，相当于 ++i
                 *            getAndAdd() - 先获取后添加，相当于i++
                 */
                totalCount.addAndGet(cartInfo.getSkuNum());

                //商品总金额累加
                totalMoney.addAndGet(price.multiply(new BigDecimal(cartInfo.getSkuNum())).doubleValue());

                //返回
                return cartInfo;
            }).collect(Collectors.toList());
            result.put("cartInfoList", cartInfoListNew);
            result.put("totalCount", totalCount);
            result.put("totalMoney", totalMoney);

            return result;
        }

        //没有选中的购物项时，直接返回null
        return null;
    }

    /**
     * 下单以后，删除用户下所有选中状态的购物项
     *
     * @return
     */
    @Override
    public Boolean deleteCheck() {
        return cartInfoMapper.delete(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, ThreadLocalUtil.get())
                        .eq(CartInfo::getIsChecked, CartConst.CART_CHECKED)) > 0;
    }
}
