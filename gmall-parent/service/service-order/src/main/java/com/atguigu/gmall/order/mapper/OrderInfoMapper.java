package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: OrderInfo的Mapper接口
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
}
