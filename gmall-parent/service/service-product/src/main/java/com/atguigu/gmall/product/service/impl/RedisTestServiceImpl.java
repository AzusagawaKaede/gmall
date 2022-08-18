package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.RedisTestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author: rlk
 * @date: 2022/8/2
 * Description: 测试在微服务和数据库之间添加redis缓存
 */
@Service
public class RedisTestServiceImpl implements RedisTestService {

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    /**
     * 测试redis实现分布式锁
     */
    @Override
    public void testRedis() {
        //尝试上锁
        try {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
            if (lock) {
                //获取到了锁
                try {
                    Integer java0217 = (Integer) redisTemplate.opsForValue().get("java0217");
                    if (java0217 != null) {
                        java0217++;
                    }
                    redisTemplate.opsForValue().set("java0217", java0217);
                } catch (Exception e) {
                    System.out.println("获取锁成功，但是业务出现异常");
                } finally {
                    //释放锁，先判断是不是自己上的锁
                    //使用Lua脚本保证获取lock的value，相等判断，释放锁三个操作的原子性
                    DefaultRedisScript script = new DefaultRedisScript();
                    script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
                    //注意：resultType必须设置，否则会报错
                    script.setResultType(Long.class);
                    /**
                     *  第一个参数：要执行的Lua脚本
                     *  第二个参数：键
                     *  第三个参数：传递的参数
                     */
                    //执行完Lua脚本即锁被释放
                    redisTemplate.execute(script, Arrays.asList("lock"), uuid);

                    //使用Java的方式释放锁不具备原子性
//                    String uuidRedis = (String) redisTemplate.opsForValue().get("lock");
//                    if(uuid.equals(uuidRedis)){
//                        //代表是自己的锁，释放掉
//                        redisTemplate.delete("lock");
//                    }
                }
            } else {
                //上锁失败，重复获取锁
                Thread.sleep(100);
                testRedis();
            }
        } catch (Exception e) {
            System.out.println("上锁失败！");
            e.printStackTrace();
        }
    }

    /**
     * 测试使用redisson
     */
    @Override
    public void testRedisson() {
        //定义锁，锁的key为lock，RLock实现了Lock接口
        RLock lock = redissonClient.getLock("lock");
        /**
         * lock.tryLock(100, 100, TimeUnit.SECONDS):
         *  第一个参数：在多少秒内一直尝试获取锁
         *  第二个和第三个参数：锁的持续时间，即超时时间
         */
        try {
            if (lock.tryLock(100, 100, TimeUnit.SECONDS)) {
                //获取到锁了，对java0217进行加一操作
                try {
                    Integer java0217 = (Integer) redisTemplate.opsForValue().get("java0217");
                    if (java0217 != null) {
                        java0217++;
                    }
                    redisTemplate.opsForValue().set("java0217", java0217);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("获取到锁了，但业务出现了异常");
                } finally {
                    //释放锁
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取锁失败！");
        }
    }
}
