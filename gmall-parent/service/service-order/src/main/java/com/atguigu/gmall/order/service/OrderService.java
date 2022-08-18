package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * @author: rlk
 * @date: 2022/8/10
 * Description: 订单模块的业务层
 */
public interface OrderService {

    /**
     * 新增订单
     * @param orderInfo
     */
    public void addOrder(OrderInfo orderInfo);

    /**
     * 取消订单
     * @param orderId
     */
    public void cancelOrder(Long orderId);

    /**
     * 修改订单状态，接收JSON字符串
     * @param result
     */
    public void updateOrder(String result);
}
