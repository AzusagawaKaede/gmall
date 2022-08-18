package com.atguigu.gmall.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.seckill.pojo.UserRecode;
import com.atguigu.gmall.seckill.service.SecKillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: rlk
 * @date: 2022/8/17
 * Description: 监听秒杀支付队列
 */
@Component
@Log4j2
public class SeckillOrderPayListener {

    @Autowired
    private SecKillOrderService secKillOrderService;

    @RabbitListener(queues = "seckill_pay_queue")
    public void seckillOrderPayListener(Channel channel, Message message){
        //获取队列里的消息result
        String result = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //修改订单状态
//            System.out.println(userRecode);
            secKillOrderService.updateSeckillOrder(result);
            //手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if(message.getMessageProperties().isRedelivered()){
                    //第二次被拒绝，记录日志然后丢弃
                    log.error("修改订单状态，报文为" + result);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次被拒绝，再来一次
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception ex){
                e.printStackTrace();
                log.error("拒绝修改订单状态消息异常，报文为" + result);
            }
        }
    }
}
