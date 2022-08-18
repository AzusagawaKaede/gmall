package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description:
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo> implements BaseAttrInfoService {

    /**
     * 条件查询
     * @param baseAttrInfo
     * @return
     */
    @Override
    public List<BaseAttrInfo> queryByCondition(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo == null) {
            //没有条件
            return baseMapper.selectList(null);
        }
        //说明有查询条件
        LambdaQueryWrapper<BaseAttrInfo> wrapper = getCondition(baseAttrInfo);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<BaseAttrInfo> queryPage(Long pageNum, Long pageSize) {
        Page<BaseAttrInfo> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, null).getRecords();
    }

    /**
     * 条件分页查询
     * @param pageNum
     * @param pageSize
     * @param baseAttrInfo
     * @return
     */
    @Override
    public List<BaseAttrInfo> queryPageByCondition(Long pageNum, Long pageSize, BaseAttrInfo baseAttrInfo) {
        LambdaQueryWrapper<BaseAttrInfo> wrapper = getCondition(baseAttrInfo);
        Page<BaseAttrInfo> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, wrapper).getRecords();
    }

    /**
     * 封装条件
     * @param baseAttrInfo
     * @return
     */
    public LambdaQueryWrapper<BaseAttrInfo> getCondition(BaseAttrInfo baseAttrInfo) {
        LambdaQueryWrapper<BaseAttrInfo> wrapper = new LambdaQueryWrapper<>();
        if (baseAttrInfo == null) {
            return null;
        }
        if (baseAttrInfo.getId() != null) {
            wrapper.eq(BaseAttrInfo::getId, baseAttrInfo.getId());
        }
        if (baseAttrInfo.getAttrName() != null) {
            wrapper.like(BaseAttrInfo::getAttrName, baseAttrInfo.getAttrName());
        }
        if (baseAttrInfo.getCategoryId() != null) {
            wrapper.eq(BaseAttrInfo::getCategoryId, baseAttrInfo.getCategoryId());
        }
        if (baseAttrInfo.getCategoryLevel() != null) {
            wrapper.eq(BaseAttrInfo::getCategoryLevel, baseAttrInfo.getCategoryLevel());
        }
        return wrapper;
    }
}
