package com.atguigu.gmall.seckill.task;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.mapper.SecKillGoodsMapper;
import com.atguigu.gmall.seckill.utils.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: rlk
 * @date: 2022/8/15
 * Description: 定时任务，用于数据预热
 */
@Component
public class SecKillGoodsFromDbToRedis {

    @Resource
    private SecKillGoodsMapper secKillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 数据预热，将最近五个时间段的数据加载进redis
     * 使用@Scheduled注解需要开启定时任务注解，在启动类上添加@EnableScheduling
     *
     * 注解属性说明：
     * fixRate：以固定时间间隔执行任务，从开始执行任务开始计算，单位是 毫秒
     * fixDelay：以固定时间间隔执行任务，从执行任务完毕开始计算， 单位是 毫秒
     * initialDelay：在服务器启动后，延迟 n 毫秒 执行任务
     *
     * cron：表达式，一共七个部分，分别代表：秒 分 时 日 月 周 年，最后的年可以省略
     * *：表示任意，即每时每刻都执行
     * -：表示在时间段内执行，如 1-10
     * /：表示在某个时间开始，固定多少间隔重复执行，如 1/10
     *
     * 这里表示：每分钟的第一秒执行任务，间隔20秒重复执行。即每20秒执行一次
     */
    @Scheduled(cron = "1/20 * * * * *")
    public void secKillGoodsFromDbToRedis(){
        //预热五个时间段的数据。
        //获取时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();
        //遍历时间菜单，查询时间段数据
        dateMenus.stream().forEach(date -> {
            //计算出平台开始时间和结束时间
            Date startTime = date;
            Date endTime = DateUtil.addDateHour(date, 2);

            //查询数据 -- 审核状态，开始时间和结束时间，库存数
            LambdaQueryWrapper<SeckillGoods> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SeckillGoods::getStatus, "1");
            wrapper.ge(SeckillGoods::getStartTime, startTime);
            wrapper.le(SeckillGoods::getEndTime, endTime);
            wrapper.gt(SeckillGoods::getStockCount, 0);
            //隐藏条件：查询的数据必须是redis没有的，否则会把redis中的数据覆盖掉
            //获取当前时间段redis数据的所有的hkey
            String key = DateUtil.data2str(date, "yyyyMMddHH");
            Set keys = redisTemplate.opsForHash().keys(key);
            wrapper.notIn(SeckillGoods::getId, keys);
            List<SeckillGoods> seckillGoodsList = secKillGoodsMapper.selectList(wrapper);

            //计算存活时间
            long liveTime = endTime.getTime() - System.currentTimeMillis();

            //遍历秒杀商品列表，保存到redis -- hash
            seckillGoodsList.stream().forEach(seckillGoods -> {
                //日期时间作为key，商品id作为hkey（必须是String），商品作为hvalue
                redisTemplate.opsForHash().put(key, seckillGoods.getId() + "", seckillGoods);
                //准备一个数组
                String[] ids = getIds(seckillGoodsList.size(), seckillGoods.getId());
                //为每个商品创建一个list，代表库存。左进右出模拟队列
                redisTemplate.opsForList().leftPushAll("Seckill_Goods_Stock_Queue_"  + seckillGoods.getId(), ids);
                //设置每个商品list的超时时间
                redisTemplate.expire("Seckill_Goods_Stock_Queue_" + seckillGoods.getId(), liveTime, TimeUnit.MILLISECONDS);
                //在redis中保存每个商品的库存，方便存取。存储为hash，按照time存储
                redisTemplate.opsForHash().put("SeckillGoodsStockCount_" + key,
                        seckillGoods.getId() + "",
                        seckillGoods.getStockCount());
            });
            //设置超时时间
            setExpire(liveTime, key);
        });

    }

    private void setExpire(long liveTime, String key) {
        //设置超时时间每个时间段设置一次就可以里，不需要重复设置
        Long increment = redisTemplate.opsForHash().increment("SeckillGoodsExpireTimes", key, 1);
        if(increment > 1){
            //说明已经设置过了，直接返回
            return;
        }
        //设置商品数据超时时间，两个小时，到点就删除
        redisTemplate.expire(key, liveTime, TimeUnit.MILLISECONDS);

        //库存数据超时时间至少是两个小时 + 订单超时时间，只要在这之后都可以
        // 这里设置不删除也是可以的，等到redis同步完库存到MySQL，再去进行删除
//        redisTemplate.expire("SeckillGoodsStockCount_" + key,
//                liveTime + 1800000,
//                TimeUnit.MILLISECONDS);

        //因此我们使用延迟队列，让死信队列的消费者去查询库存，同步，然后清空redis。因此我们的消息就是key
        //同时我们写在设置过期时间的方法里也可以保证每个时间段只会触发一次，只会发送一次消息
        rabbitTemplate.convertAndSend("seckill_goods_normal_exchange",
                "seckill.goods.normal",
                key,
                ((message -> {
                    //设置过期时间
                    MessageProperties messageProperties = message.getMessageProperties();
                    messageProperties.setExpiration(liveTime +1800000 + "");
//                    messageProperties.setExpiration("30000");   //测试使用30秒
                    return message;
                })));
    }

    /**
     * 自动生成商品list列表。这里使用数组使用是因为数组可以随机存取效率更高
     * @param count
     * @param goodsId
     * @return
     */
    private String[] getIds(int count, Long goodsId) {
        String[] ids = new String[count];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = goodsId + "";
        }
        return ids;
    }
}
