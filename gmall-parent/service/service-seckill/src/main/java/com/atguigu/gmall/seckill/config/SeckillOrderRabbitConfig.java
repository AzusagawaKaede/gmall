package com.atguigu.gmall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀订单的消息队列
 */
@Configuration
public class SeckillOrderRabbitConfig {

    /**
     * 秒杀订单的交换机
     * @return
     */
    @Bean("seckillOrderExchange")
    public Exchange seckillOrderExchange(){
        return ExchangeBuilder.directExchange("seckill_order_exchange").build();
    }

    /**
     * 秒杀订单的队列
     * @return
     */
    @Bean("seckillOrderQueue")
    public Queue seckillOrderQueue(){
        return QueueBuilder.durable("seckill_order_queue").build();
    }

    /**
     * 秒杀订单的交换机和队列的绑定
     * @param seckillOrderExchange
     * @param seckillOrderQueue
     * @return
     */
    @Bean
    public Binding seckillOrderBinding(@Qualifier("seckillOrderExchange") Exchange seckillOrderExchange,
                                       @Qualifier("seckillOrderQueue") Queue seckillOrderQueue){
        return BindingBuilder.bind(seckillOrderQueue).to(seckillOrderExchange)
                .with("seckill.order.add").noargs();
    }
}
