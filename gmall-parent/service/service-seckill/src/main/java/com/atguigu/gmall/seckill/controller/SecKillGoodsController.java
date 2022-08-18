package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.seckill.service.SecKillGoodsService;
import com.atguigu.gmall.seckill.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀页面相关数据查询的控制层
 */
@RestController
@RequestMapping("/api/seckill/goods")
public class SecKillGoodsController {

    @Autowired
    private SecKillGoodsService secKillGoodsService;

    /**
     * 查询指定时间段秒杀商品列表
     * @param time
     * @return
     */
    @GetMapping("/getSecKillGoods")
    public Result getSecKillGoods(String time){
        return Result.ok(secKillGoodsService.getSeckillGoods(time));
    }

    /**
     * 查询指定秒杀商品详情
     * @param time
     * @param goodsId
     * @return
     */
    @GetMapping("/getSingleSecKillGoods")
    public Result getSingleSecKillGoods(String time, String goodsId){
        return Result.ok(secKillGoodsService.getSeckillGoods(time, goodsId));
    }

    /**
     * 获取时间菜单
     * @return
     */
    @GetMapping("/getDateMenus")
    public Result getDateMenus(){
        return Result.ok(DateUtil.getDateMenus());
    }
}
