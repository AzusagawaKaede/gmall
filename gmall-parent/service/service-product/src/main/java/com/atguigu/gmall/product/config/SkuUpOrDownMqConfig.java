package com.atguigu.gmall.product.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/12
 * Description: 配置商品上下架使用的RabbitMQ
 */
@Configuration
@Log4j2
public class SkuUpOrDownMqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 设置rabbitTemplate的return回调
     */
//    @Bean
//    public void rabbitTemplateConfig() {
//        //设置可靠性投递 -- return回调
//        rabbitTemplate.setReturnCallback((message, code, text, exchange, routingKey) -> {
//            String skuId = new String(message.getBody());
//            if (routingKey.equals("sku_up")) {
//                log.error("商品" + skuId + "上架失败！");
//            } else if (routingKey.equals("sku_down")) {
//                log.error("商品" + skuId + "下架失败！");
//            }
//            log.error("失败状态码：" + code);
//            log.error("失败的原因：" + text);
//            log.error("使用的交换机：" + exchange);
//            log.error("使用的路由：" + routingKey);
//        });
//    }

    /**
     * 创建一个直连交换机
     *
     * @return
     */
    @Bean("skuUpOrDownExchange")
    public Exchange skuUpOrDownExchange() {
        return ExchangeBuilder.directExchange("sku_up_or_down_exchange").build();
    }

    /**
     * 用于商品上架的队列
     *
     * @return
     */
    @Bean("skuUpQueue")
    public Queue skuUpQueue() {
        return QueueBuilder.durable("sku_up_queue").build();
    }

    /**
     * 用于商品下架的队列
     *
     * @return
     */
    @Bean("skuDownQueue")
    public Queue skuDownQueue() {
        return QueueBuilder.durable("sku_down_queue").build();
    }

    /**
     * 绑定上架关系
     *
     * @param skuUpOrDownExchange
     * @param skuUpQueue
     * @return
     */
    @Bean
    public Binding bindingSkuUp(@Qualifier("skuUpOrDownExchange") Exchange skuUpOrDownExchange,
                                @Qualifier("skuUpQueue") Queue skuUpQueue) {
        return BindingBuilder.bind(skuUpQueue).to(skuUpOrDownExchange).with("sku_up").noargs();
    }

    /**
     * 绑定下架关系
     *
     * @param skuUpOrDownExchange
     * @param skuDownQueue
     * @return
     */
    @Bean
    public Binding bindingSkuDown(@Qualifier("skuUpOrDownExchange") Exchange skuUpOrDownExchange,
                                  @Qualifier("skuDownQueue") Queue skuDownQueue) {
        return BindingBuilder.bind(skuDownQueue).to(skuUpOrDownExchange).with("sku_down").noargs();
    }
}
