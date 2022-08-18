package com.atguigu.gmall.product.service;

/**
 * @author: rlk
 * @date: 2022/8/2
 * Description: 测试在微服务与数据库之间添加redis缓存
 */
public interface RedisTestService {

    /**
     * 测试redis实现分布式锁
     */
    public void testRedis();

    /**
     * 测试使用redisson
     */
    public void testRedisson();
}
