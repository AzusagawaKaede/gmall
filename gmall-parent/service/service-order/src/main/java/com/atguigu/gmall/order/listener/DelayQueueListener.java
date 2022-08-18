package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: rlk
 * @date: 2022/8/13
 * Description: 监听私信队列的消费者
 */
@Component
@Log4j2
public class DelayQueueListener {

    @Autowired
    private OrderService orderService;

    /**
     * 监听死信队列
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "order_dead_queue")
    public void delayQueueListener(Channel channel, Message message){
        //获取队列里的消息orderId
        String orderId = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //取消订单
            orderService.cancelOrder(Long.valueOf(orderId));
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                if(message.getMessageProperties().isRedelivered()){
                    //第二次被拒绝
                    log.error("取消订单失败，订单号为：" + orderId);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次被拒绝
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception ex){
                log.error("取消订单出现异常，原因为：" + e.getMessage());
            }
        }
    }
}
