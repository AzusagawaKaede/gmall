package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description:
 */
public interface BaseAttrValueService extends IService<BaseAttrValue> {

    /**
     * Mybatis-plus提供的方法：
     *  新增，修改，删除，查询所有，根据id查询
     */

    /**
     * 根据条件查询
     * @param baseAttrValue
     * @return
     */
    List<BaseAttrValue> queryByCondition(BaseAttrValue baseAttrValue);

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<BaseAttrValue> queryPage(Long pageNum, Long pageSize);

    /**
     * 条件分页查询
     * @param pageNum
     * @param pageSize
     * @param baseAttrValue
     * @return
     */
    List<BaseAttrValue> queryPageByCondition(Long pageNum, Long pageSize, BaseAttrValue baseAttrValue);
}
