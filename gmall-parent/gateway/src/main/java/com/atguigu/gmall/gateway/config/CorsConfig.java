package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author: rlk
 * @date: 2022/7/28
 * Description:
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {

        // cors跨域配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        //设置允许访问的网络
        configuration.addAllowedOrigin("*");
        // 设置是否从服务器获取cookie
        configuration.setAllowCredentials(true);
        // 设置请求方法 * 表示任意
        configuration.addAllowedMethod("*");
        // 所有请求头信息 * 表示任意
        configuration.addAllowedHeader("*");

        // 配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);
        // cors过滤器对象
        return new CorsWebFilter(configurationSource);
    }
}
