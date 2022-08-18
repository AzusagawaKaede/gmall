package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.util.HttpClient;
import com.atguigu.gmall.payment.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/13
 * Description: 微信支付的业务层实现类
 *  不涉及到数据库操作不需要声明事务
 *
 *  注意：@ConfigurationProperties(prefix = "weixin.pay") 注入值必须生成getter()和setter()！！！
 */
@Service
//@ConfigurationProperties(prefix = "weixin.pay")
public class WxPayServiceImpl implements WxPayService {

    @Value("${weixin.pay.appid}")
    private String appId;
    @Value("${weixin.pay.partner}")
    private String partner;
    @Value("${weixin.pay.partnerkey}")
    private String partnerKey;
    @Value("${weixin.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 获取微信二维码的url
     * @param reqMap：前端传递的所有参数，使用map接收。包含orderId，desc，money，exchange，routingKey，username
     * @return
     */
    @Override
    public String getQrCodeUrl(Map<String, String> reqMap) {
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        //构建参数，map->xml
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("mch_id", partner);
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        params.put("body", reqMap.get("desc"));
        params.put("out_trade_no", reqMap.get("orderId"));
        params.put("total_fee", reqMap.get("money"));
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "NATIVE");
        //添加附加参数
        //附加数据
        Map<String, String> attchMap = new HashMap<>();
        attchMap.put("exchange", reqMap.get("exchange"));
        attchMap.put("routingKey", reqMap.get("routingKey"));
        //判断用户名是否为空
        if(!StringUtils.isEmpty(reqMap.get("username"))){
            attchMap.put("username", reqMap.get("username"));
        }
        //保存附加数据
        params.put("attach", JSONObject.toJSONString(attchMap));
        try {
            String xmlParams = WXPayUtil.generateSignedXml(params, partnerKey);

            //发起请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParams);
            httpClient.post();

            //获得返回数据，xml格式
            String content = httpClient.getContent();

            //解析返回数据
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            if(result.get("return_code").equals("SUCCESS")){
                if(result.get("result_code").equals("SUCCESS")){
                    return result.get("code_url");
                }
            }
            return JSONObject.toJSONString(result);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单状态
     *
     * @param orderId
     * @return
     */
    @Override
    public String getOrderStatus(String orderId) {
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        //拼接参数
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("mch_id", partner);
        params.put("out_trade_no", orderId);
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //发起请求
            String xmlParams = WXPayUtil.generateSignedXml(params, partnerKey);
            HttpClient httpClient = new HttpClient(url);
            httpClient.setXmlParam(xmlParams);
            httpClient.setHttps(true);
            httpClient.post();
            //获取结果并解析
            String content = httpClient.getContent();
            Map<String, String> result = WXPayUtil.xmlToMap(content);
            if(result.get("return_code").equals("SUCCESS")){
                if(result.get("result_code").equals("SUCCESS")){
                    if (result.get("trade_state").equals("SUCCESS")){
                        return result.get("trade_state");
                    }
                }
            }
            return JSONObject.toJSONString(result);
        }catch (Exception e){
            
        }
        return null;
    }
}
