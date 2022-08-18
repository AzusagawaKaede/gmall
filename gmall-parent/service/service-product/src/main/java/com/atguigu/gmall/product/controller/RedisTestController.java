package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.RedisTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: rlk
 * @date: 2022/8/2
 * Description:
 */
@RestController
@RequestMapping("/admin/product")
public class RedisTestController {

    @Autowired
    private RedisTestService redisTestService;

    @GetMapping("/test")
    public Result test(){
        redisTestService.testRedis();
        return Result.ok();
    }

    @GetMapping("/testRedisson")
    public Result testRedisson(){
        redisTestService.testRedisson();
        return Result.ok();
    }
}
