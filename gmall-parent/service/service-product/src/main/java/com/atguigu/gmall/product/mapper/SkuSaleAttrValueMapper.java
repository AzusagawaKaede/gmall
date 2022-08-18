package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/7/30
 * Description:
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    /**
     * 查询页面切换所需要的数据
     * @param spuId
     * @return
     */
    List<Map> getSkuSaleAttrValueBySpuId(Long spuId);
}
