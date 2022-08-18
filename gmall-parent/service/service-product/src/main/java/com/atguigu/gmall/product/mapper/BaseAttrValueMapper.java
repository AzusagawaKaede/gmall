package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description: BaseAttrValue的Mapper接口
 */
@Mapper
public interface BaseAttrValueMapper extends BaseMapper<BaseAttrValue> {

    /**
     * 根据一级分类id，二级分类id，三级分类id获取BaseAttrInfo
     * @param category1Id
     * @param category2Id
     * @param category3Id
     * @return
     */
    List<BaseAttrValue> selectBaseAttrInfoByCategoryId(@Param("category1Id") Long category1Id,
                                                       @Param("category2Id") Long category2Id,
                                                       @Param("category3Id") Long category3Id);

}
