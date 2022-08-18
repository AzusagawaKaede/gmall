package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description: BaseAttrInfo的Mapper接口
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {

    /**
     * 根据skuId查询平台属性和对应的值
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId);
}
