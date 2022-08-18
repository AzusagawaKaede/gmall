package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrValueService;
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
public class BaseAttrValueServiceImpl extends ServiceImpl<BaseAttrValueMapper, BaseAttrValue> implements BaseAttrValueService {

    /**
     * 条件查询
     * @param baseAttrValue
     * @return
     */
    @Override
    public List<BaseAttrValue> queryByCondition(BaseAttrValue baseAttrValue) {
        if (baseAttrValue == null) {
            //条件为空，返回所有
            return baseMapper.selectList(null);
        }
        LambdaQueryWrapper wrapper = getCondition(baseAttrValue);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public List<BaseAttrValue> queryPage(Long pageNum, Long pageSize) {
        Page<BaseAttrValue> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, null).getRecords();
    }

    /**
     * 条件分页查询
     * @param pageNum
     * @param pageSize
     * @param baseAttrValue
     * @return
     */
    @Override
    public List<BaseAttrValue> queryPageByCondition(Long pageNum, Long pageSize, BaseAttrValue baseAttrValue) {
        Page<BaseAttrValue> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper wrapper = getCondition(baseAttrValue);
        return baseMapper.selectPage(page, wrapper).getRecords();
    }

    /**
     * 封装条件
     * @param baseAttrValue
     * @return
     */
    public LambdaQueryWrapper getCondition(BaseAttrValue baseAttrValue){
        LambdaQueryWrapper<BaseAttrValue> wrapper = new LambdaQueryWrapper<>();
        if (baseAttrValue == null) {
            return null;
        }
        if (baseAttrValue.getId() != null) {
            wrapper.eq(BaseAttrValue::getId, baseAttrValue.getId());
        }
        if (baseAttrValue.getValueName() != null) {
            wrapper.like(BaseAttrValue::getValueName, baseAttrValue.getValueName());
        }
        if (baseAttrValue.getAttrId() != null) {
            wrapper.eq(BaseAttrValue::getAttrId, baseAttrValue.getAttrId());
        }
        return wrapper;
    }
}
