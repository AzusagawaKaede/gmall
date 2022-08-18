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
 * @date: 2022/8/14
 * Description: 普通订单消息队列的监听
 */
@Component
@Log4j2
public class OrderPayQueueListener {

    @Autowired
    private OrderService orderService;

    /**
     * 普通订单消息队列的监听
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "order_pay_queue")
    public void orderPayQueueListener(Channel channel, Message message){
        //获取队列里的消息result
        String result = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //修改订单状态
            orderService.updateOrder(result);
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                if(message.getMessageProperties().isRedelivered()){
                    //第二次被拒绝
                    log.error("修改订单状态失败，消息为：" + result);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次被拒绝
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception ex){
                log.error("修改订单状态出现异常，原因为：" + e.getMessage());
            }
        }
    }
}
