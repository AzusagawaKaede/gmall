package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/29
 * Description:
 */
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据spuId查询销售属性
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> querySpuSaleAttrBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据spuId查询spu所有的销售属性（包括销售属性对应的所有销售属性值）
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(@Param("spuId") Long spuId, @Param("skuId") Long skuId);
}
