package com.atguigu.gmall.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.seckill.pojo.UserRecode;
import com.atguigu.gmall.seckill.service.SecKillGoodsService;
import com.atguigu.gmall.seckill.service.SecKillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * @author: rlk
 * @date: 2022/8/16
 * Description: 库存数据同步到MySQL的消费者
 */
@Component
@Log4j2
public class SeckillGoodsFromRedisToDbListener {

    @Autowired
    private SecKillGoodsService secKillGoodsService;

    @RabbitListener(queues = "seckill_goods_dead_queue")
    public void seckillGoodsFromRedisToDbListener(Channel channel, Message message){
        //获取队列里的消息 -- 时间段key（time）
        String time = new String(message.getBody());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //调用service，获取redis中的库存同步到MySQL
//            System.out.println("time = " + time);
            secKillGoodsService.mergeSeckillGoodsStockToDb(time);
            //手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                if(message.getMessageProperties().isRedelivered()){
                    //第二次被拒绝，记录日志然后丢弃
                    log.error("redis库存同步MySQL失败，失败的时间段为：" + time);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次被拒绝，再来一次
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception ex){
                e.printStackTrace();
                log.error("拒绝消息异常，redis库存同步MySQL失败，失败的时间段为：" + time);
            }
        }
    }
}
