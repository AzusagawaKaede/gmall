package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description: 用户收货地址模块的业务层
 */
public interface UserAddressService {

    /**
     * 根据用户id查询收货地址列表
     * @return
     */
    List<UserAddress> getUserAddressListByUserId();

}
