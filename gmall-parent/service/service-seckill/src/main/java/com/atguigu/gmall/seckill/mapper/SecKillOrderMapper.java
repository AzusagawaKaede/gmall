package com.atguigu.gmall.seckill.mapper;

import com.atguigu.gmall.seckill.pojo.SeckillOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀订单的mapper接口
 */
@Mapper
public interface SecKillOrderMapper extends BaseMapper<SeckillOrder> {
}
