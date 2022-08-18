package com.atguigu.gmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description:
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
/**
 * @EnableFeignClients 的 basePackages：
 *      没有明确指明basePackages的路径
 *      则spring ioc不会自动为外部引入的其他服务jar包里,标注了@FeignClient注解的interface自动生成bean对象
 *  这里我们引入了service-product-client，如果不指定basePackages，则不会创建其中的ProduceFeignService对象
 */
@EnableFeignClients(basePackages = "com.atguigu.gmall")
public class ItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }
}
