package com.atguigu.gmall.list.listener;

import com.atguigu.gmall.list.service.GoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: rlk
 * @date: 2022/8/12
 * Description:
 */
@Component
@Log4j2
public class SkuUpOrDownListener {

    @Autowired
    private GoodsService goodsService;

    /**
     * 监听上架队列
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "sku_up_queue")
    public void skuUp(Channel channel, Message message) {
        //获取skuId
        Long skuId = Long.parseLong(new String(message.getBody()));
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //执行上架
            goodsService.addGoodsToEs(skuId);
            //没有异常则确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //有异常则拒绝消息
                if (message.getMessageProperties().isRedelivered()) {
                    //说明是第二次失败，打印日志然后丢弃
                    log.error("商品skuId上架失败，失败的原因是：" + e.getMessage());
                    channel.basicReject(deliveryTag, false);
                } else {
                    //说明是第一次失败，重新放回队列
                    channel.basicReject(deliveryTag, true);
                }
            } catch (Exception ex) {
                log.error("拒绝商品" + skuId + "上架失败，原因是：" + e.getMessage());
            }
        }
    }

    /**
     * 监听下架队列
     *
     * @param channel
     * @param message
     */
    @RabbitListener(queues = "sku_down_queue")
    public void skuDown(Channel channel, Message message) {
        //获取skuId
        Long skuId = Long.parseLong(new String(message.getBody()));
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            //执行下架
            goodsService.deleteGoods(skuId);
            //没有异常则确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //有异常则拒绝消息
                if (message.getMessageProperties().isRedelivered()) {
                    //说明是第二次失败，打印日志然后丢弃
                    log.error("商品skuId下架失败，失败的原因是：" + e.getMessage());
                    channel.basicReject(deliveryTag, false);
                } else {
                    //说明是第一次失败，重新放回队列
                    channel.basicReject(deliveryTag, true);
                }
            } catch (Exception ex) {
                log.error("拒绝商品" + skuId + "下架失败，原因是：" + e.getMessage());
            }
        }
    }
}
