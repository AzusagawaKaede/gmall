package com.atguigu.gmall.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: cart微服务提供给order微服务的feign接口
 */
@FeignClient(name = "service-cart", path = "/api/cart", contextId = "service-cart")
public interface CartFeignService {

    /**
     * 查询用户选中状态下的购物项的接口，提供给feign使用，和提供给用户使用的Controller分开
     * @return
     */
    @GetMapping("/addOrder")
    public Map addOrder();

    /**
     * 用户下单后，删除选中状态的购物项
     * @return
     */
    @DeleteMapping
    public Boolean deleteCheck();

}
