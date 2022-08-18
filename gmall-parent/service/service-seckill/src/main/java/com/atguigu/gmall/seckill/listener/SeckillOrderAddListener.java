package com.atguigu.gmall.seckill.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.seckill.pojo.UserRecode;
import com.atguigu.gmall.seckill.service.SecKillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 监听器，用来消费下单消息
 */
@Configuration
@Log4j2
public class SeckillOrderAddListener {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SecKillOrderService secKillOrderService;

    @RabbitListener(queues = "seckill_order_queue")
    public void seckillOrderAddListener(Channel channel, Message message) {
        //获取队列里的消息result
        String result = new String(message.getBody());
        //反序列化
        UserRecode userRecode = JSONObject.parseObject(result, UserRecode.class);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //真实下单
//            System.out.println(userRecode);
            secKillOrderService.realSeckillOrderAdd(userRecode);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //下单失败，秒杀只给一次机会。修改排队实体状态并写回实体
                userRecode.setStatus(3);
                userRecode.setMsg(e.getMessage() + "，请重试");
                String username = userRecode.getUsername();
                //更新用户排队状态
                redisTemplate.opsForValue().set("User_Recode_" + username, userRecode);
                //删除用来保证不会重复提交的key
                redisTemplate.delete("User_Queue_Count_" + username);

                //打印日志
                log.error("用户" + username + "秒杀下单失败，原因为：" + e.getMessage());
                e.printStackTrace();
                channel.basicReject(deliveryTag, false);
            } catch (Exception ex) {
                log.error("拒绝消费下单消息异常, 消息为:" + result);
            }
        }
    }
}
