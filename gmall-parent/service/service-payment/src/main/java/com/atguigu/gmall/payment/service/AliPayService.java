package com.atguigu.gmall.payment.service;

/**
 * @author: rlk
 * @date: 2022/8/14
 * Description: 支付宝支付的业务层
 */
public interface AliPayService {

    /**
     * 获取支付页面
     * @param orderId
     * @param desc
     * @param money
     * @return
     */
    public String getPayPageUrl(String orderId, String desc, String money);

    /**
     * 主动查询订单状态
     * @param orderId
     * @return
     */
    public String getOrderStatus(String orderId);
}
