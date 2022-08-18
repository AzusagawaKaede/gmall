package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.Goods;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/5
 * Description:
 */
public interface GoodsService {

    /**
     * 根据skuId将商品写入到Es
     * @param skuId
     */
    public void addGoodsToEs(Long skuId);

    /**
     * 根据goodsId删除Goods
     * @param goodsId
     */
    public void deleteGoods(Long goodsId);

    /**
     * 根据goodsId添加商品的热点分数
     * @param goodsId
     */
    public void addHotScore(Long goodsId);
}