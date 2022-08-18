package com.atguigu.gmall.seckill.mapper;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀商品的mapper接口
 */
@Mapper
public interface SecKillGoodsMapper extends BaseMapper<SeckillGoods> {

    /**
     * 根据id修改秒杀商品的库存
     * @param id
     * @param stock
     * @return
     */
    @Update("update seckill_goods set stock_count = #{stock} where id = #{id}")
    int updateStock(@Param("id") Long id,
                    @Param("stock") Integer stock);

}
