package com.atguigu.gmall.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: rlk
 * @date: 2022/8/3
 * Description:
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.atguigu.gmall.list.client", "com.atguigu.gmall.item.client", "com.atguigu.gmall.product.client"})
@EnableDiscoveryClient
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
