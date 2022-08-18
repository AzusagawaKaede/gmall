package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀页面相关数据查询的业务层，秒杀相关的查询，基本都是从redis查询
 * 秒杀具有高并发的特点，如果直接查询MySQL，数据库会崩掉
 */
public interface SecKillGoodsService {

    /**
     * 查询指定时间段的商品数据
     * @param time：格式 yyyyMMddHH
     * @return
     */
    public List<SeckillGoods> getSeckillGoods(String time);

    /**
     * 查询指定商品详情
     * @param time
     * @param goodsId
     * @return
     */
    public SeckillGoods getSeckillGoods(String time, String goodsId);

    /**
     * 将redis中对应时间的商品库存信息同步到MySQL
     * @param time
     */
    public void mergeSeckillGoodsStockToDb(String time);
}
