package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.utils.ThreadLocalUtil;
import com.atguigu.gmall.cart.utils.TokenUtil;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.atguigu.gmall.common.constant.CartConst.CART_CHECKED;
import static com.atguigu.gmall.common.constant.CartConst.CART_UNCHECKED;

/**
 * @author: rlk
 * @date: 2022/8/9
 * Description: 购物车功能的控制层
 * 使用购物车功能必须先登录，因此在购物车功能里，所有的操作必须要验证token里的username
 * 防止当前用户操作非当前用户的数据！！
 */
@RestController
@RequestMapping("/api/cart")
public class CartInfoController {

    @Autowired
    private CartInfoService cartInfoService;

    /**
     * 添加购物车 -- 修改购物车商品数量
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/addCartInfo")
    public Result addCartInfo(Long skuId, Integer skuNum) {
        cartInfoService.addCartInfo(skuId, skuNum);
        return Result.ok();
    }

    /**
     * 删除购物车商品
     * @param cartInfoId
     * @return
     */
    @DeleteMapping("/deleteCartInfo")
    public Result deleteCartInfo(Long cartInfoId){
        cartInfoService.deleteCartInfo(cartInfoId);
        return Result.ok();
    }

    /**
     * 根据token里的username，查询cartInfo列表
     * @return
     */
    @GetMapping("/getCartInfoList")
    public Result getCartInfoList(HttpServletRequest request){
        List<CartInfo> cartInfoList = cartInfoService.getCartInfoList();
        return Result.ok(cartInfoList);
    }

    /**
     * 将购物项选中，有cartInfoId是选中单个，没有是选中当前userId的所有
     * @param cartInfoId
     * @return
     */
    @PutMapping("/checked")
    public Result checked(Long cartInfoId){
        cartInfoService.checkedOrUnchecked(cartInfoId, CART_CHECKED);
        return Result.ok();
    }

    /**
     * 将购物项不选中，有cartInfoId是选中单个，没有是选中当前userId的所有
     * @param cartInfoId
     * @return
     */
    @PutMapping("/unchecked")
    public Result unchecked(Long cartInfoId){
        cartInfoService.checkedOrUnchecked(cartInfoId, CART_UNCHECKED);
        return Result.ok();
    }

    /**
     * 查询用户本次生成订单的相关购物项信息，数据都使用token从数据库中查询
     * @return
     */
    @GetMapping("/getOrderConfirm")
    public Result getOrderConfirm(){
        return Result.ok(cartInfoService.getOrderConfirm());
    }
}
