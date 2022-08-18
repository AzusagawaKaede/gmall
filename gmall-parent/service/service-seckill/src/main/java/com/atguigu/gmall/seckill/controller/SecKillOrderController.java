package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.seckill.service.SecKillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀订单的控制层
 */
@RestController
@RequestMapping("/api/seckill/order")
public class SecKillOrderController {

    @Autowired
    private SecKillOrderService secKillOrderService;

    /**
     * 新增秒杀订单。伪新增，真排队
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    @GetMapping("/addSecKillOrder")
    public Result addSecKillOrder(String time, String goodsId, String num){
        return Result.ok(secKillOrderService.addSecKillOrder(time, goodsId, num));
    }

    /**
     * 查询redis中的排队实体，根据token中的username
     * @return
     */
    @GetMapping("/getUserRecode")
    public Result getUserRecode(){
        return Result.ok(secKillOrderService.getUserRecode());
    }
}
