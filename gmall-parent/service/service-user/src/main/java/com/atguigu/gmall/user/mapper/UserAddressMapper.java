package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: rlk
 * @date: 2022/8/8
 * Description: 用户收货地址模块的Mapper层
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
