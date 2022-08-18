package com.atguigu.gmall.order.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: rlk
 * @date: 2022/8/14
 * Description: 支付宝客户端的配置类
 */
@Configuration
public class AliPayClientConfig {

    @Value("${ali.alipayUrl}")
    private String alipayUrl;

    @Value("${ali.appId}")
    private String appId;

    @Value("${ali.appPrivateKey}")
    private String appPrivateKey;

    @Value("${ali.alipayPublicKey}")
    private String alipayPublicKey;

    @Value("${ali.returnPaymentUrl}")
    private String returnPaymentUrl;

    @Value("${ali.notifyPaymentUrl}")
    private String notifyPaymentUrl;

    @Bean
    public AlipayClient defaultAliPayClient(){
        //初始化客户端
        return new DefaultAlipayClient(
                "https://openapi.alipay.com/gateway.do",
                appId,
                appPrivateKey,
                "json",
                "utf-8",
                alipayPublicKey,
                "RSA2");
    }
}
