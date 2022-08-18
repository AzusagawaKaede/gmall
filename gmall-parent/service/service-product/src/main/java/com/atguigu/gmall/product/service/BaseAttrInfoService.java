package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description: BasAttrInfo的Service接口
 */
public interface BaseAttrInfoService extends IService<BaseAttrInfo> {

    /**
     * 新增
     * Mybatis-plus已经有写好的新增方法
     */

    /**
     * 删除
     * Mybatis-plus已经有写好的删除方法
     */

    /**
     * 修改
     * Mybatis-plus已经有写好的修改方法
     */

    /**
     * 根据id查询
     * Mybatis-plus已经有写好的根据id查询的方法
     */

    /**
     * 查询全部
     * Mybatis-plus已经有写好的查询全部的方法
     */

    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    List<BaseAttrInfo> queryByCondition(BaseAttrInfo baseAttrInfo);

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<BaseAttrInfo> queryPage(Long pageNum, Long pageSize);


    /**
     * 分页条件查询
     * @param pageNum
     * @param pageSize
     * @param baseAttrInfo
     * @return
     */
    List<BaseAttrInfo> queryPageByCondition(Long pageNum, Long pageSize, BaseAttrInfo baseAttrInfo);

}
