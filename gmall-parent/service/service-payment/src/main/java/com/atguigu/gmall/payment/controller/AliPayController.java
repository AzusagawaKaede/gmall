package com.atguigu.gmall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.payment.service.AliPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/14
 * Description: 支付宝支付的控制层
 */
@RestController
@RequestMapping("/ali/pay")
public class AliPayController {

    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取支付页面
     * @param orderId
     * @param desc
     * @param money
     * @return
     */
    @GetMapping("/getPayPageUrl")
    public String getPayPageUrl(String orderId, String desc, String money){
        return aliPayService.getPayPageUrl(orderId, desc, money);
    }

    /**
     * 主动查询订单结果
     * @param orderId
     * @return
     */
    @RequestMapping("/getOrderStatus")
    public String getOrderStatus(String orderId){
        return aliPayService.getOrderStatus(orderId);
    }

    /**
     * 支付宝同步回调的接口，该接口返回结果不能作为支付依据
     * 支付返回的参数是JSON格式，直接使用Map获取
     *
     * @param returnMap
     * @return
     */
    @RequestMapping("/callback/return")
    public String returnCallback(@RequestParam Map<String, String> returnMap) {
        String returnJson = JSONObject.toJSONString(returnMap);
//        System.out.println("json = " + json);
        return returnJson;
    }

    /**
     * 支付宝异步回调返回交易结果
     * @param notifyMap
     * @return
     */
    @RequestMapping("/callback/notify")
    public String notifyCallback(@RequestParam Map<String, String> notifyMap) {
        String notifyJson = JSONObject.toJSONString(notifyMap);
//        System.out.println("json = " + json);
        //向map添加支付方式
        notifyMap.put("payWay", "1");
        //发送修改订单状态的消息到MQ
        rabbitTemplate.convertAndSend("pay_exchange", "pay.order", notifyJson);
        return "success";
    }
}
