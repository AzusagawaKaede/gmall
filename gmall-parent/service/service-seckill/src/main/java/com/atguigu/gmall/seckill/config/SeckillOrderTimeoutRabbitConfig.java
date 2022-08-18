package com.atguigu.gmall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/17
 * Description: 秒杀订单超时使用的MQ配置类
 */
@Configuration
public class SeckillOrderTimeoutRabbitConfig {

    /**
     * 创建正常交换机
     * @return
     */
    @Bean("seckillOrderNormalExchange")
    public Exchange seckillOrderNormalExchange(){
        return ExchangeBuilder.directExchange("seckill_order_normal_exchange").build();
    }

    /**
     * 创建正常队列
     * @return
     */
    @Bean("seckillOrderNormalQueue")
    public Queue seckillOrderNormalQueue(){
        return QueueBuilder.durable("seckill_order_normal_queue")
                //设置死信交换机和路由key
                .deadLetterExchange("seckill_order_dead_exchange")
                .deadLetterRoutingKey("seckill.order.dead")
                .build();
    }

    /**
     * 绑定正常交换机和正常队列
     * @param seckillOrderNormalExchange
     * @param seckillOrderNormalQueue
     * @return
     */
    @Bean
    public Binding seckillOrderNormalBinding(@Qualifier("seckillOrderNormalExchange") Exchange seckillOrderNormalExchange,
                                             @Qualifier("seckillOrderNormalQueue") Queue seckillOrderNormalQueue){
        return BindingBuilder.bind(seckillOrderNormalQueue).to(seckillOrderNormalExchange)
                .with("seckill.order.normal").noargs();
    }

    /**
     * 创建死信交换机
     * @return
     */
    @Bean("seckillOrderDeadExchange")
    public Exchange seckillOrderDeadExchange(){
        return ExchangeBuilder.directExchange("seckill_order_dead_exchange").build();
    }

    /**
     * 创建死信队列
     * @return
     */
    @Bean("seckillOrderDeadQueue")
    public Queue seckillOrderDeadQueue(){
        return QueueBuilder.durable("seckill_order_dead_queue").build();
    }

    /**
     * 绑定死信交换机和死信队列
     * @param seckillOrderDeadExchange
     * @param seckillOrderDeadQueue
     * @return
     */
    @Bean
    public Binding seckillOrderDeadQBinding(@Qualifier("seckillOrderDeadExchange") Exchange seckillOrderDeadExchange,
                                            @Qualifier("seckillOrderDeadQueue") Queue seckillOrderDeadQueue){
        return BindingBuilder.bind(seckillOrderDeadQueue).to(seckillOrderDeadExchange)
                .with("seckill.order.dead").noargs();
    }
}
