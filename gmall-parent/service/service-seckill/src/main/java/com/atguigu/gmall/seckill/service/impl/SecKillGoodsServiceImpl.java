package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.mapper.SecKillGoodsMapper;
import com.atguigu.gmall.seckill.service.SecKillGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀页面相关数据查询的业务层实现类，秒杀相关的查询，基本都是从redis查询
 * 秒杀具有高并发的特点，如果直接查询MySQL，数据库会崩掉
 */
@Service
@Log4j2
public class SecKillGoodsServiceImpl implements SecKillGoodsService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private SecKillGoodsMapper secKillGoodsMapper;

    /**
     * 查询指定时间段的商品数据
     *
     * @param time：格式 yyyyMMddHH
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoods(String time) {
        //参数校验
        if (StringUtils.isEmpty(time)) {
            throw new RuntimeException("参数错误");
        }
        //查询数据并返回
        return redisTemplate.opsForHash().values(time);
    }

    /**
     * 查询指定商品详情
     *
     * @param time
     * @param goodsId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGoods(String time, String goodsId) {
        //参数校验
        if (StringUtils.isEmpty(time) || StringUtils.isEmpty(goodsId)) {
            throw new RuntimeException("参数错误");
        }
        return (SeckillGoods) redisTemplate.opsForHash().get(time, goodsId);
    }

    /**
     * 将redis中对应时间的商品库存信息同步到MySQL
     *
     * @param time
     */
    @Override
    public void mergeSeckillGoodsStockToDb(String time) {
        //从redis中查询数据
        //查询时间段所有的商品id的集合
        Set<String> keys = redisTemplate.opsForHash().keys(("SeckillGoodsStockCount_" + time));
        //非空判断
        if (keys != null && keys.size() > 0) {
            //遍历写入到MySQL
            keys.stream().forEach(goodsId -> {
                try {
                    //查询库存
                    Integer stock = (Integer) redisTemplate.opsForHash()
                            .get("SeckillGoodsStockCount_" + time, goodsId);
                    //写入到MySQL
                    int update = secKillGoodsMapper.updateStock(Long.parseLong(goodsId), stock);
                    if (update < 0) {
                        //记录到日志
                        log.error("redis同步库存到MySQL失败，失败的商品id为：" + goodsId);
                        return;
                    }
                    //记录成功，删除redis中的key
                    redisTemplate.opsForHash().delete("SeckillGoodsStockCount_" + time, goodsId);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("redis同步库存到MySQL失败，失败的商品id为：" + goodsId);
                }
            });
        }
    }
}
