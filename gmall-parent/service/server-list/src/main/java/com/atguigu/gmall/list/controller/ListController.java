package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.GoodsService;
import com.atguigu.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: rlk
 * @date: 2022/8/5
 * Description: 商品上架下架时调用的接口，保存到Es/从Es中删除
 */
@RestController
@RequestMapping("/api/list")
public class ListController {

    //ElasticSearch的reset风格的模板，除此之外还有ElasticSearchTemplate
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private GoodsService goodsService;

    /**
     * SpringBoot下创建索引
     * @return
     */
    @RequestMapping("/createIndex")
    public Result createIndex(){
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    /**
     * 根据skuId将商品插入到Es
     * @param skuId
     * @return
     */
    @RequestMapping("/addGoodsToEs/{skuId}")
    public String addGoodsToEs(@PathVariable Long skuId){
        goodsService.addGoodsToEs(skuId);
        return "添加成功";
    }

    /**
     * 根据id从Es删除商品
     * @param goodsId
     * @return
     */
    @RequestMapping("/deleteGoods/{goodsId}")
    public String deleteGoods(@PathVariable Long goodsId){
        goodsService.deleteGoods(goodsId);
        return "删除成功";
    }

    /**
     * 商品热点+1
     * @param goodsId
     * @return
     */
    @RequestMapping("/addHotScore/{goodsId}")
    public String addHotScore(@PathVariable Long goodsId){
        goodsService.addHotScore(goodsId);
        return "热点+1成功";
    }
}
