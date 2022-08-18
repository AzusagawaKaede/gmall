package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author: rlk
 * @date: 2022/7/30
 * Description:
 */
@Mapper
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    /**
     * 扣减商品库存
     * @param skuId
     * @param stock
     * @return
     */
    @Update("update sku_info set stock = stock - #{stock} where id = #{skuId} and stock >= #{stock}")
    int decountStock(@Param("skuId") Long skuId,
                     @Param("stock") Integer stock);

    /**
     * 回滚库存
     * @param skuId
     * @param stock
     * @return
     */
    @Update("update sku_info set stock = stock + #{stock} where id = #{skuId}")
    int rollbackStock(@Param("skuId") Long skuId,
                      @Param("stock") Integer stock);
}
