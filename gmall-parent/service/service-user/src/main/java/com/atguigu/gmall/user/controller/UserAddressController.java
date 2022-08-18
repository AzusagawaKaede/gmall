package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description:
 */
@RestController
@RequestMapping("/api/user")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /**
     * 查询用户收货地址
     * @return
     */
    @GetMapping("/getUserAddressListByUserId")
    public List<UserAddress> getUserAddressListByUserId(){
        return userAddressService.getUserAddressListByUserId();
    }
}
