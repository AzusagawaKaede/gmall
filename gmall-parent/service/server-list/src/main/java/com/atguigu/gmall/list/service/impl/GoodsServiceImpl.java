package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.service.GoodsService;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: rlk
 * @date: 2022/8/5
 * Description:
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 根据skuId将商品写入到Es
     *
     * @param skuId
     */
    @Override
    public void addGoodsToEs(Long skuId) {
        //参数校验
        if (skuId == null) {
            throw new RuntimeException("参数错误");
        }

        //查询商品信息 -- server-product的Feign
        SkuInfo skuInfo = productFeignService.getSkuInfoBySkuId(skuId);

        //校验查询的商品信息
        if (skuInfo == null || skuInfo.getId() == null) {
            throw new RuntimeException("商品不存在");
        }

        //查询价格
        BigDecimal price = productFeignService.getPriceBySkuId(skuId);
        //查询分类信息
        BaseCategoryView baseCategoryView = productFeignService.getBaseCategoryViewById(skuId);
        //查询品牌信息
        BaseTrademark baseTrademark = productFeignService.getBaseTrademarkById(skuInfo.getTmId());
        //查询平台属性
        List<BaseAttrInfo> baseAttrList = productFeignService.getBaseAttrInfoBySkuId(skuInfo.getId());

        //将商品信息封装到Goods
        Goods goods = new Goods();
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
//        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(price.doubleValue());
        goods.setCreateTime(new Date());
        goods.setTmId(skuInfo.getTmId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setCategory1Id(baseCategoryView.getCategory1Id());
        goods.setCategory1Name(baseCategoryView.getCategory1Name());
        goods.setCategory2Id(baseCategoryView.getCategory2Id());
        goods.setCategory2Name(baseCategoryView.getCategory2Name());
        goods.setCategory3Id(baseCategoryView.getCategory3Id());
        goods.setCategory3Name(baseCategoryView.getCategory3Name());
        List<SearchAttr> attrs = baseAttrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(attrs);

        goodsDao.save(goods);
    }

    /**
     * 根据goodsId删除Goods
     *
     * @param goodsId
     */
    @Override
    public void deleteGoods(Long goodsId) {
        goodsDao.deleteById(goodsId);
    }

    /**
     * 根据goodsId添加商品的热点分数
     *
     * @param goodsId
     */
    @Override
    public void addHotScore(Long goodsId) {
        //参数校验
        if (goodsId == null) {
            throw new RuntimeException("参数错误");
        }
        //查询商品
        Optional<Goods> optional = goodsDao.findById(goodsId);
        if (optional == null) {
            //商品不存在
            return;
        } else {
            //获取到商品的score，保存到redis。redis是线程安全的，score是加1后的值
            Double score =
                    redisTemplate.opsForZSet().
                            incrementScore("score", "hot:score:" + goodsId, 1);
            Goods goods = optional.get();
            if(score.longValue() % 10 == 0){
                goods.setHotScore(score.longValue());
                goodsDao.save(goods);
            }

        }
    }
}
