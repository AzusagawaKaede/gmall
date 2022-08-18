package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.payment.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/14
 * Description: 支付宝支付的业务层
 */
@Service
public class AliPayServiceImpl implements AliPayService {


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

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 获取支付页面
     *
     * @param orderId
     * @param desc
     * @param money
     * @return
     */
    @Override
    public String getPayPageUrl(String orderId, String desc, String money) {

        //初始化客户端
//        AlipayClient alipayClient = new DefaultAlipayClient(
//                "https://openapi.alipay.com/gateway.do",
//                appId,
//                appPrivateKey,
//                "json",
//                "utf-8",
//                alipayPublicKey,
//                "RSA2");
        //拼接请求参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyPaymentUrl);
        request.setReturnUrl(returnPaymentUrl);
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", money);
        bizContent.put("subject", desc);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        try {
            //发起请求
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            //返回请求结果
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 主动查询订单状态
     *
     * @param orderId
     * @return
     */
    @Override
    public String getOrderStatus(String orderId) {
        //初始化客户端
//        AlipayClient alipayClient = new DefaultAlipayClient(
//                "https://openapi.alipay.com/gateway.do",
//                appId,
//                appPrivateKey,
//                "json",
//                "utf-8",
//                alipayPublicKey,
//                "RSA2");
        //拼接请求参数 -- 订单号
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        request.setBizContent(bizContent.toString());
        try {
            //发起请求
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            //返回结果
            return response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

}
