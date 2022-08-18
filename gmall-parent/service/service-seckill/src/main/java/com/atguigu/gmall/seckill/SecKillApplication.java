package com.atguigu.gmall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 秒杀微服务的启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.atguigu.gmall")
@EnableScheduling
@ServletComponentScan(basePackages = "com.atguigu.gmall.seckill.filter")
public class SecKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecKillApplication.class, args);
    }
}
