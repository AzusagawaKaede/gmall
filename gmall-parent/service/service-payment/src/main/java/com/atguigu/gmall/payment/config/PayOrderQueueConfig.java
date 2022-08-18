package com.atguigu.gmall.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/14
 * Description: 普通订单的消息队列配置类
 */
@Configuration
public class PayOrderQueueConfig {

    /**
     * 普通订单的交换机
     * @return
     */
    @Bean("payExchange")
    public Exchange payExchange(){
        return ExchangeBuilder.directExchange("pay_exchange").build();
    }

    /**
     * 普通订单的队列
     * @return
     */
    @Bean("orderPayQueue")
    public Queue orderPayQueue(){
        return QueueBuilder.durable("order_pay_queue").build();
    }

    /**
     * 秒杀订单的队列
     * @return
     */
    @Bean("seckillPayQueue")
    public Queue seckillPayQueue(){
        return QueueBuilder.durable("seckill_pay_queue").build();
    }


    /**
     * 普通订单交换机和队列的绑定
     * @param payExchange
     * @param orderPayQueue
     * @return
     */
    @Bean
    public Binding orderPayBinding(@Qualifier("payExchange") Exchange payExchange,
                                   @Qualifier("orderPayQueue") Queue orderPayQueue){
        return BindingBuilder.bind(orderPayQueue).to(payExchange).with("pay.order").noargs();
    }

    @Bean
    public Binding seckillPayBinding(@Qualifier("payExchange") Exchange payExchange,
                                     @Qualifier("seckillPayQueue") Queue seckillPayQueue){
        return BindingBuilder.bind(seckillPayQueue).to(payExchange).with("pay.seckill").noargs();
    }

}
