package com.atguigu.gmall.payment.service;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/13
 * Description: 微信支付的业务层
 *  1 获取微信二维码的url
 *  2 根据订单号主动查询订单状态
 *  3 微信异步回调接口
 */
public interface WxPayService {

    /**
     * 获取微信二维码的url
     * @param reqMap：前端传递的所有参数，使用map接收
     * @return
     */
    public String getQrCodeUrl(Map<String, String> reqMap);

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    public String getOrderStatus(String orderId);

}
