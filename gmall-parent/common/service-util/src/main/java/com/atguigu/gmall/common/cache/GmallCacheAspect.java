package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Around("@annotation(com.atguigu.gmall.common.cache.Java0217Cache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {

        /*
        1.  获取参数列表
        2.  获取方法上的注解
        3.  获取前缀
        4.  获取目标方法的返回值
         */
        Object result = null;
        try {
            //获取切入点方法的参数
            Object[] args = point.getArgs();
            //获取方法签名，通过签名可以获取方法上的注解
            MethodSignature signature = (MethodSignature) point.getSignature();
            /**
             * 通过方法签名获取指定注解
             * Java0217Cache java0217Cache = signature.getMethod().getAnnotation(Java0217Cache.class);
             * 在Java0217Cache注解类中，我使用了@AliasFor，设置value为prefix的别名
             * 但是@AliasFor是Spring提供的注解，Java原生通过signature.getMethod().getAnnotation()方法并不支持
             * 使用时获取到的value是null从而抛出异常。因此使用Spring提供的获取注解值的工具类AnnotationUtils
             */
            Java0217Cache java0217Cache = AnnotationUtils.getAnnotation(signature.getMethod(), Java0217Cache.class);
            //通过方法签名前缀
            String prefix = java0217Cache.prefix();
            //拼接key，从缓存中获取数据
            String key = prefix + Arrays.asList(args).toString();

            //从redis中获取缓存数据
            result = cacheHit(signature, key);
            if (result != null) {
                //缓存有数据，直接返回
                return result;
            }
            //初始化分布式锁，注意分布式锁的key不能和要查询的key一样，查询的key存储的value是具体的数据，而redisson分布式锁的key的value存放的是线程Id。
            //key一致的话会导致第一次查询数据，写入到redis后，释放锁失败
            RLock lock = redissonClient.getLock(key + ":lock");
            //尝试获取锁
            boolean flag = lock.tryLock(100, 100, TimeUnit.SECONDS);
            if (flag) {
                //获取锁成功，第一层try...catch用来保证锁一定被释放
                try {
                    //第二层try...catch用来保证业务的正常处理
                    try {
                        //再去redis中查询一边，判断是否其他线程已经查出来了
                        result = cacheHit(signature, key);
                        if (result != null) {
                            return result;
                        }
                        //执行切入点方法从数据库中查询数据
                        result = point.proceed(point.getArgs());
                        if (result == null) {
                            //创建一个Object放入redis，防止缓存穿透
                            result = new Object();
                            this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 300, TimeUnit.SECONDS);
                        } else {
                            //查询到数据了，同样结果放入缓存
                            this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 24, TimeUnit.HOURS);
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //释放锁
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 从redis中获取指定key数据并反序列化
     *
     * @param signature
     * @param key
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        //根据key查询缓存
        String cache = (String) redisTemplate.opsForValue().get(key);
        //如果查询出来的cache不为空，说明有值，对其反序列化并直接返回
        if (StringUtils.isNotBlank(cache)) {
            // 获取方法返回类型
            Class returnType = signature.getReturnType();
            return JSONObject.parseObject(cache, returnType);
        }
        //cache为空，返回null
        return null;
    }

}
