package com.atguigu.gmall.seckill.service;

/**
 * @author: rlk
 * @date: 2022/8/16
 * Description: 测试的业务层
 */
public interface TestService {

    /**
     * 测试主线程出现异常时，CompletableFuture子线程对数据库的操作是否回滚
     * 不会回滚
     */
    public void test();
}
