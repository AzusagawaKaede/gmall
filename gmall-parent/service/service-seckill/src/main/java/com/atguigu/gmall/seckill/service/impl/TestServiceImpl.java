package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.seckill.mapper.SecKillOrderMapper;
import com.atguigu.gmall.seckill.pojo.SeckillOrder;
import com.atguigu.gmall.seckill.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: rlk
 * @date: 2022/8/16
 * Description: 测试的业务层实体类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestServiceImpl implements TestService {
    @Resource
    private SecKillOrderMapper secKillOrderMapper;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 测试主线程出现异常时，CompletableFuture子线程对数据库的操作是否回滚
     * 不会回滚
     */
    @Override
    public void test() {
        //异步将订单信息保存，写入MySQL
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(UUID.randomUUID().toString().replace("-", ""));
        seckillOrder.setGoodsId("100");
        seckillOrder.setNum(100);
        seckillOrder.setMoney("100");
        seckillOrder.setUserId("100");
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");
        CompletableFuture<Boolean> future1 = CompletableFuture.supplyAsync(() -> {
            secKillOrderMapper.insert(seckillOrder);
            return true;
        }, threadPoolExecutor).exceptionally(ex -> {
            ex.printStackTrace();
            return false;
        });
        try {
            System.out.println("future1.get() = " + future1.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("回滚");
    }
}
