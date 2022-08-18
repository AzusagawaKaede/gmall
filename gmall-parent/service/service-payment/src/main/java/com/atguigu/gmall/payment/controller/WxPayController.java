package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.payment.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/13
 * Description:
 */
@RestController
@RequestMapping("/wx/pay")
public class WxPayController {

    @Autowired
    private WxPayService wxPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取微信支付二维码
     * @param reqMap：前端传递的所有参数，使用map接收
     * @return
     */
    @GetMapping("/getQrCodeUrl")
    public String getQrCodeUrl(@RequestParam Map<String, String> reqMap) {
        return wxPayService.getQrCodeUrl(reqMap);
    }

    /**
     * 主动查询订单状态
     *
     * @param orderId
     * @return
     */
    @GetMapping("/getOrderStatus")
    public String getOrderStatus(String orderId) {
        return wxPayService.getOrderStatus(orderId);
    }

    /**
     * 给微信使用的回调接口，微信返回数据是直接返回的数据流，需要我们自己处理
     *
     * @param request
     * @return
     */
    @RequestMapping("/callback/notify")
    public String wxReturnCallback(HttpServletRequest request) throws Exception {
        //获取数据流
//        ServletInputStream is = request.getInputStream();
//        //准备一个数组接收
//        byte[] bys = new byte[1024];
//        //定义一个StringBuffer存储数据
//        StringBuffer sb = new StringBuffer();
//        //处理接受的流数据
//        int len;
//        while ((len = is.read(bys)) != -1) {
//            sb.append(new String(bys, 0, len));
//        }
//        //解析结果，结果也是xml格式
//        /**
//         * {"transaction_id":"4200001561202208174296054193","nonce_str":"7f45e492018544a6aad1db64be711b98","bank_type":"OTHERS","openid":"oHwsHuI0ClIG52Ay-QYabEcAFNwA","sign":"7EA73EB565D34DC61C39F38AB62F898B","fee_type":"CNY","mch_id":"1558950191","cash_fee":"1","out_trade_no":"937be683bf7d49dd9b193a12f1ac6914","appid":"wx74862e0dfcf69954","total_fee":"1","trade_type":"NATIVE","result_code":"SUCCESS","attach":"{\"exchange\":\"pay_exchange\",\"routingKey\":\"pay.seckill\",\"username\":\"rlk\"}","time_end":"20220817230000","is_subscribe":"N","return_code":"SUCCESS"}
//         */
//        Map<String, String> result = WXPayUtil.xmlToMap(sb.toString());

        String resultString = "{\"transaction_id\":\"4200001561202208174296054193\",\"nonce_str\":\"7f45e492018544a6aad1db64be711b98\",\"bank_type\":\"OTHERS\",\"openid\":\"oHwsHuI0ClIG52Ay-QYabEcAFNwA\",\"sign\":\"7EA73EB565D34DC61C39F38AB62F898B\",\"fee_type\":\"CNY\",\"mch_id\":\"1558950191\",\"cash_fee\":\"1\",\"out_trade_no\":\"c29d64438b204b22a83cae237b262229\",\"appid\":\"wx74862e0dfcf69954\",\"total_fee\":\"1\",\"trade_type\":\"NATIVE\",\"result_code\":\"SUCCESS\",\"attach\":\"{\\\"exchange\\\":\\\"pay_exchange\\\",\\\"routingKey\\\":\\\"pay.seckill\\\",\\\"username\\\":\\\"rlk\\\"}\",\"time_end\":\"20220817230000\",\"is_subscribe\":\"N\",\"return_code\":\"SUCCESS\",\"payWay\":\"0\"}";
        Map<String, String> result = JSONObject.parseObject(resultString, Map.class);
        String attachString = result.get("attach");
        Map<String, String> attachMap = JSONObject.parseObject(attachString, Map.class);
        rabbitTemplate.convertAndSend(attachMap.get("exchange"),
                attachMap.get("routingKey"),
                JSONObject.toJSONString(result));

        //返回结果
        HashMap<String, String> returnMap = new HashMap<>();
        returnMap.put("return_code", "SUCCESS");
        returnMap.put("return_msg", "OK");
        return WXPayUtil.mapToXml(returnMap);
    }

}
