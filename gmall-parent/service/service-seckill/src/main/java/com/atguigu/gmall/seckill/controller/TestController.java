package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.seckill.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: rlk
 * @date: 2022/8/16
 * Description: 测试的控制层
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    /**
     * 测试主线程出现异常时，CompletableFuture子线程对数据库的操作是否回滚
     * 不会回滚
     * @return
     */
    @GetMapping
    public Result test(){
        testService.test();
        return Result.ok();
    }
}
