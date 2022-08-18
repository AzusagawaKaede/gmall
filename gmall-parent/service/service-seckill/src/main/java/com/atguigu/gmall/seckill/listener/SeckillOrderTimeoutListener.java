package com.atguigu.gmall.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.seckill.pojo.UserRecode;
import com.atguigu.gmall.seckill.service.SecKillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.aspectj.weaver.ast.Or;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: rlk
 * @date: 2022/8/17
 * Description: 秒杀订单超时队列的消费者
 */
@Component
@Log4j2
public class SeckillOrderTimeoutListener {

    @Autowired
    private SecKillOrderService secKillOrderService;

    @RabbitListener(queues = "seckill_order_dead_queue")
    public void seckillOrderTimeoutListener(Channel channel, Message message){
        //获取队列里的消息orderId
        String result = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //获取订单id，取消订单
//            System.out.println(result);
            secKillOrderService.cancelSeckillOrder(result);
            //手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //只有一次机会
                channel.basicReject(deliveryTag, false);
            } catch (Exception ex) {
                log.error("拒绝消费取消订单消息异常, 订单号为:" + result);
            }
        }
    }
}
