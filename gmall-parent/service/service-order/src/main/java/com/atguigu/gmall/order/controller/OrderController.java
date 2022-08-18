package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: 订单模块的控制层
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/addOrder")
    public Result addOrder(@RequestBody OrderInfo orderInfo){
        orderService.addOrder(orderInfo);
        return Result.ok();
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @DeleteMapping("/cancelOrder")
    public Result cancelOrder(Long orderId){
        //防止订单重复取消
        Long increment = redisTemplate.opsForValue().increment("user_cancle_order_count_" + orderId);
        redisTemplate.expire("user_cancle_order_count_" + orderId, 10, TimeUnit.SECONDS);
        if(increment > 1){
            return Result.ok();
        }
        try {
            orderService.cancelOrder(orderId);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            redisTemplate.delete("user_cancle_order_count_" + orderId);
        }
        return Result.ok();
    }

}
