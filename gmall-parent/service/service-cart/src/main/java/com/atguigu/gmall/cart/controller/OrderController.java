package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: 提供给order微服务使用远程feign接口的控制层
 */
@RestController
@RequestMapping("/api/cart")
public class OrderController {

    @Autowired
    private CartInfoService cartInfoService;

    /**
     * 查询用户选中状态下的购物项的接口，提供给feign使用，和提供给用户使用的Controller分开
     * @return
     */
    @GetMapping("/addOrder")
    public Map addOrder(){
        return cartInfoService.getOrderConfirm();
    }

    /**
     * 用户下单后，删除选中状态的购物项
     * @return
     */
    @DeleteMapping
    public Boolean deleteCheck(){
        return cartInfoService.deleteCheck();
    }
}
