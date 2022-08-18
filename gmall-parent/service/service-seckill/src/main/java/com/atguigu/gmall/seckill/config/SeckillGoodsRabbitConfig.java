package com.atguigu.gmall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/16
 * Description: 秒杀商品同步库存使用的延迟队列配置
 */
@Configuration
public class SeckillGoodsRabbitConfig {

    /**
     * 创建正常交换机
     * @return
     */
    @Bean("seckillGoodsNormalExchange")
    public Exchange seckillGoodsNormalExchange(){
        return ExchangeBuilder.directExchange("seckill_goods_normal_exchange").build();
    }

    /**
     * 创建正常队列
     * @return
     */
    @Bean("seckillGoodsNormalQueue")
    public Queue seckillGoodsNormalQueue(){
        return QueueBuilder.durable("seckill_goods_normal_queue")
                //绑定死信交换机
                .deadLetterExchange("seckill_goods_dead_exchange")
                //绑定死信交换机的路由key
                .deadLetterRoutingKey("seckill.goods.dead")
                .build();
    }

    /**
     * 绑定正常交换机和正常队列
     * @param seckillGoodsNormalExchange
     * @param seckillGoodsNormalQueue
     * @return
     */
    @Bean
    public Binding seckillGoodsNormalBinding(@Qualifier("seckillGoodsNormalExchange") Exchange seckillGoodsNormalExchange,
                                             @Qualifier("seckillGoodsNormalQueue") Queue seckillGoodsNormalQueue){
        return BindingBuilder.bind(seckillGoodsNormalQueue).to(seckillGoodsNormalExchange)
                .with("seckill.goods.normal").noargs();
    }

    /**
     * 创建死信交换机
     * @return
     */
    @Bean("seckillGoodsDeadExchange")
    public Exchange seckillGoodsDeadExchange(){
        return ExchangeBuilder.directExchange("seckill_goods_dead_exchange").build();
    }

    /**
     * 创建死信队列
     * @return
     */
    @Bean("seckillGoodsDeadQueue")
    public Queue seckillGoodsDeadQueue(){
        return QueueBuilder.durable("seckill_goods_dead_queue").build();
    }

    /**
     * 绑定死信交换机和死信队列
     * @param seckillGoodsDeadExchange
     * @param seckillGoodsDeadQueue
     * @return
     */
    @Bean
    public Binding seckillGoodsDeadBinding(@Qualifier("seckillGoodsDeadExchange") Exchange seckillGoodsDeadExchange,
                                           @Qualifier("seckillGoodsDeadQueue") Queue seckillGoodsDeadQueue){
        return BindingBuilder.bind(seckillGoodsDeadQueue).to(seckillGoodsDeadExchange)
                .with("seckill.goods.dead").noargs();
    }
}
