package com.atguigu.gmall.user.client;

import com.atguigu.gmall.model.user.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: service-user的feign接口
 */
@FeignClient(name = "service-user", path = "/api/user", contextId = "service-user")
public interface UserFeignService {

    /**
     * 查询用户收货地址
     * @return
     */
    @GetMapping("/getUserAddressListByUserId")
    public List<UserAddress> getUserAddressListByUserId();
}
