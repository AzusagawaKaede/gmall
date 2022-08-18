package com.atguigu.gmall.product;

import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author: rlk
 * @date: 2022/7/27
 * Description: 商品管理微服务的启动类
 */
@Log4j2
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gmall")
@EnableFeignClients(basePackages = "com.atguigu.gmall.list.client")
public class ProductApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ProductApplication.class, args);
        setRabbitTemplateCallback(ctx);
    }

    private static void setRabbitTemplateCallback(ConfigurableApplicationContext ctx) {
        RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);
        rabbitTemplate.setReturnCallback((message, code, text, exchange, routingKey) -> {
            String skuId = new String(message.getBody());
            if (routingKey.equals("sku_up")) {
                log.error("商品" + skuId + "上架失败！");
            } else if (routingKey.equals("sku_down")) {
                log.error("商品" + skuId + "下架失败！");
            }
            log.error("失败状态码：" + code);
            log.error("失败的原因：" + text);
            log.error("使用的交换机：" + exchange);
            log.error("使用的路由：" + routingKey);
        });
    }
}
