package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/9
 * Description: 购物车功能的业务层
 *      使用购物车功能必须先登录，因此在购物车功能里，所有的操作必须要验证token里的username
 *      防止当前用户操作非当前用户的数据！！
 */
public interface CartInfoService {

    /**
     * 添加商品到购物车 -- 修改购物车商品的数量
     * @param skuId
     * @param skuNum
     */
    public void addCartInfo(Long skuId, Integer skuNum);

    /**
     * 购物车删除购物项，根据id删除但是还要判断username是否和id对应的userId一致
     * @param cartInfoId
     */
    public void deleteCartInfo(Long cartInfoId);

    /**
     * 根据userId获取购物车列表
     * @return
     */
    public List<CartInfo> getCartInfoList();

    /**
     * 修改username下一个或所有的cartInfo的选中状态
     * @param cartInfoId
     * @param status
     */
    public void checkedOrUnchecked(Long cartInfoId, Short status);

    /**
     * 查询用户本次生成订单的相关购物项信息，数据都使用token从数据库中查询
     * @return
     */
    public Map<String, Object> getOrderConfirm();

    /**
     * 下单以后，删除用户下所有选中状态的购物项
     * @return
     */
    public Boolean deleteCheck();
}