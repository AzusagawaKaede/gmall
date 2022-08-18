package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ApiItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: feign使用的service
 */
@Slf4j
@Service
public class ApiItemServiceImpl implements ApiItemService {

    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private BaseTrademarkMapper baseTrademarkMapper;
    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 根据skuId查询skuInfo
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoBySkuId(Long skuId) {
        return skuInfoMapper.selectById(skuId);
    }

    /**
     * 根据skuId从Redis中查询skuInfo
     *
     * @param skuId
     * @return
     */
    @Override
    @Deprecated
    public SkuInfo getSkuInfoBySkuIdFromRedis(Long skuId) {
        //参数校验，这里接口时内部调用，且调用feign时已经判断了，这里不需要再判断了
        if (skuId == null) {
            return null;
        }
        SkuInfo skuInfo = null;
        String key = "skuInfo::" + skuId;

        //尝试从redis中获取skuInfo，以skuInfo::skuId为key
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);

        //如果skuInfo为null，则去数据库中查询。但是此时我们只允许一个请求去数据库中查询
        //这里不判断skuInfo.getId()的原因是后面如果数据库中没有查询到记录我们会new skuInfo()，new出来的id是null
        if (skuInfo == null) {
            //上锁，只允许一个请求去查询数据库
            RLock lock = redissonClient.getLock("skuInfoLock");
            try {
                if (lock.tryLock(100, 100, TimeUnit.SECONDS)) {
                    try {
                        //获取到锁了，首先再查一遍判断skuInfo是不是为null。万一上一个进入此代码的线程已经查询出来了
                        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
                        if (skuInfo == null) {
                            skuInfo = skuInfoMapper.selectById(skuId);
                        }
                    } catch (Exception e) {
                        log.error("请求获取RLock锁成功，但业务出现了异常。异常信息为" + e.getMessage());
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("请求获取RLock锁失败，失败原因为" + e.getMessage());
            }
        }

        //判断是否查询到了
        if (skuInfo == null || skuInfo.getId() == null) {
            //没有查询到，则new一个skuInfo放入redis并设置五分钟的超时时间，防止缓存穿透
            skuInfo = new SkuInfo();
            redisTemplate.opsForValue().set(key, skuInfo, 300, TimeUnit.SECONDS);
        } else {
            //查询到了，则放入redis（也需要设置超时时间）并返回
            redisTemplate.opsForValue().set(key, skuInfo, 3600 * 24, TimeUnit.SECONDS);
//            return skuInfo;
        }
        return skuInfo;
    }

    /**
     * 根据skuId从Redis中查询skuInfo
     * 改进
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoBySkuIdFromRedis2(Long skuId) {
        if (skuId == null) {
            return null;
        }

        String key = "skuInfo::" + skuId;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);

        //不为null直接返回，加快命中缓存时请求响应的速度。避免出现getSkuInfoBySkuIdFromRedis(skuId)命中缓存时还判断了多个if
        if (skuInfo != null) {
            return skuInfo;
        }

        //redis中不存在的情况。依然是上锁，只允许一个请求去查询数据库
        RLock lock = redissonClient.getLock("skuInfoLock:" + skuId);
        try {
            //尝试获取锁
            if (lock.tryLock(100, 100, TimeUnit.SECONDS)) {
                try {
                    //获取到锁了，首先再查一遍判断skuInfo是不是为null。万一上一个进入此代码的线程已经查询出来了
                    //同样是不为null直接返回，加快非null时请求响应速度
                    skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
                    if (skuInfo != null) {
                        return skuInfo;
                    }
                    //去数据库中查询
                    skuInfo = skuInfoMapper.selectById(skuId);

                    //判断是否查询到了，放进锁里执行避免并发问题
                    if (skuInfo == null || skuInfo.getId() == null) {
                        skuInfo = new SkuInfo();
                        redisTemplate.opsForValue().set(key, skuInfo, 300, TimeUnit.SECONDS);
                    } else {
                        redisTemplate.opsForValue().set(key, skuInfo, 3600 * 24, TimeUnit.SECONDS);
                    }
                    return skuInfo;
                } catch (Exception e) {
                    log.error("请求获取RLock锁成功，但业务出现了异常。异常信息为" + e.getMessage());
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("请求获取RLock锁失败，失败原因为" + e.getMessage());
        }

        return null;
    }

    /**
     * 根据三级分类id查询对应的一级分类、二级分类
     *
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getBaseCategoryViewById(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 根据skuId查询对应的图片列表
     *
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getSkuImageListBySkuId(Long skuId) {
        return skuImageMapper.selectList(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, skuId));
    }

    /**
     * 根据skuId查询价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getPriceBySkuId(Long skuId) {
        return skuInfoMapper.selectById(skuId).getPrice();
    }

    /**
     * 根据spuId查询spu所有的销售属性（包括销售属性对应的所有销售属性值）
     *
     * @param spuId
     * @param skuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(Long spuId, Long skuId) {
        return spuSaleAttrMapper.getSpuSaleAttrBySpuIdAndSkuId(spuId, skuId);
    }

    /**
     * 查询页面切换所需要的数据
     *
     * @param spuId
     * @return
     */
    @Override
    public Map getSkuSaleAttrValueBySpuId(Long spuId) {
        List<Map> list = skuSaleAttrValueMapper.getSkuSaleAttrValueBySpuId(spuId);
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();
        list.stream().forEach(item -> {
            map.put(item.get("value_id"), item.get("sku_id"));
        });
        return map;
    }

    /**
     * 根据id查询品牌信息
     *
     * @param id
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademarkById(Long id) {
        return baseTrademarkMapper.selectById(id);
    }

    /**
     * 根据skuId获取商品的平台属性
     * 虽然每个商品的平台属性有多个，但是每个商品的平台属性的值只有一个
     * 所以每个BaseAttrInfo的List<BaseAttrValue>有且仅有一个
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId) {
        return baseAttrInfoMapper.getBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 用户购买商品时，删除库存
     *
     * @param map
     * @return
     */
    @Override
    public Boolean decreaseStock(Map<String, Object> map) {
        map.entrySet().stream().forEach(entry -> {
            Long skuId = Long.valueOf(entry.getKey());
            Integer stock = Integer.valueOf(entry.getValue().toString());
            int update = skuInfoMapper.decountStock(skuId, stock);
            if (update < 0){
                throw new RuntimeException("库存不足，新增订单失败");
            }
        });
        return true;
    }

    /**
     * 回滚库存
     *
     * @param skuParam
     * @return
     */
    @Override
    public Boolean rollbackStock(Map<String, Object> skuParam) {
        skuParam.entrySet().stream().forEach(entry -> {
            int update = skuInfoMapper.rollbackStock(Long.valueOf(entry.getKey()), Integer.valueOf(entry.getValue().toString()));
            if(update <= 0){
                throw new RuntimeException("回滚库存失败");
            }
        });
        return true;
    }
}
