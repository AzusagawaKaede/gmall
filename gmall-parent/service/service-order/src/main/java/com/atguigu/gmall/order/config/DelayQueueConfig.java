package com.atguigu.gmall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/13
 * Description: 延迟队列，用来处理订单超时
 */
@Configuration
public class DelayQueueConfig {

    /**
     * 正常交换机
     * @return
     */
    @Bean("orderNormalExchange")
    public Exchange orderNormalExchange(){
        return ExchangeBuilder.directExchange("order_normal_exchange").build();
    }

    /**
     * 正常队列
     * @return
     */
    @Bean("orderNormalQueue")
    public Queue orderNormalQueue(){
        return QueueBuilder.durable("order_normal_queue")
                //设置队列统一超时时间 -- 测试设置10秒
                .ttl(10 * 1000)
                //设置死信交换机和死信队列
                .deadLetterExchange("order_dead_exchange")
                .deadLetterRoutingKey("dead")
                .build();
    }

    /**
     * 正常交换机和正常队列的绑定
     * @param orderNormalExchange
     * @param orderNormalQueue
     * @return
     */
    @Bean
    public Binding orderNormal(@Qualifier("orderNormalExchange") Exchange orderNormalExchange,
                               @Qualifier("orderNormalQueue") Queue orderNormalQueue){
        return BindingBuilder.bind(orderNormalQueue).to(orderNormalExchange)
                .with("normal").noargs();
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean("orderDeadExchange")
    public Exchange orderDeadExchange(){
        return ExchangeBuilder.directExchange("order_dead_exchange").build();
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("orderDeadQueue")
    public Queue orderDeadQueue(){
        return QueueBuilder.durable("order_dead_queue").build();
    }

    /**
     * 死信交换机和死信队列的绑定
     * @param orderDeadExchange
     * @param orderDeadQueue
     * @return
     */
    @Bean
    public Binding orderDead(@Qualifier("orderDeadExchange") Exchange orderDeadExchange,
                             @Qualifier("orderDeadQueue") Queue orderDeadQueue){
        return BindingBuilder.bind(orderDeadQueue).to(orderDeadExchange).with("dead").noargs();
    }
}
