package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.seckill.pojo.UserRecode;

import java.util.concurrent.ExecutionException;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀商品订单的业务层
 */
public interface SecKillOrderService {

    /**
     * 新增秒杀订单
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    public UserRecode addSecKillOrder(String time, String goodsId, String num);

    /**
     * 查询redis中排队实体，通过token中的username
     * @return
     */
    public UserRecode getUserRecode();

    /**
     * 真正下单的接口
     */
    public void realSeckillOrderAdd(UserRecode userRecode) throws Exception;

    /**
     * 根据订单id取消订单
     * @param username
     */
    public void cancelSeckillOrder(String username);

    /**
     * 修改订单的状态
     * @param result
     */
    public void updateSeckillOrder(String result);

}
