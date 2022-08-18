package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.cache.Java0217Cache;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ApiItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: feign使用的controller
 */
@RestController
@RequestMapping("/api/item")
public class ApiItemController {

    @Autowired
    private ApiItemService apiItemService;

    /**
     * 根据skuId查询skuInfo
     *
     * @param skuId
     * @return
     */
    @Java0217Cache("getSkuInfoBySkuId:")
    @GetMapping("/getSkuInfoBySkuId/{skuId}")
    public SkuInfo getSkuInfoBySkuId(@PathVariable Long skuId) {
        return apiItemService.getSkuInfoBySkuId(skuId);
    }

    /**
     * 根据skuId从redis中查询skuInfo
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfoBySkuIdFromRedis/{skuId}")
    public SkuInfo getSkuInfoBySkuIdFromRedis(@PathVariable Long skuId) {
        return apiItemService.getSkuInfoBySkuIdFromRedis(skuId);
    }

    /**
     * 根据skuId从redis中查询skuInfo
     * 改进
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfoBySkuIdFromRedis2/{skuId}")
    public SkuInfo getSkuInfoBySkuIdFromRedis2(@PathVariable Long skuId) {
        return apiItemService.getSkuInfoBySkuIdFromRedis2(skuId);
    }

    /**
     * 根据三级分类id查询对应的一级分类、二级分类
     *
     * @param category3Id
     * @return
     */
    @Java0217Cache("getBaseCategoryViewById:")
    @GetMapping("/getBaseCategoryViewById/{category3Id}")
    public BaseCategoryView getBaseCategoryViewById(@PathVariable Long category3Id) {
        return apiItemService.getBaseCategoryViewById(category3Id);
    }

    /**
     * 根据skuId查询对应的图片列表
     *
     * @param skuId
     * @return
     */
    @Java0217Cache("getSkuImageListBySkuId:")
    @GetMapping("/getSkuImageListBySkuId/{skuId}")
    public List getSkuImageListBySkuId(@PathVariable Long skuId) {
        return apiItemService.getSkuImageListBySkuId(skuId);
    }

    /**
     * 根据skuId查询价格
     *
     * @param skuId
     * @return
     */
    @Java0217Cache("getPriceBySkuId:")
    @GetMapping("/getPriceBySkuId/{skuId}")
    public BigDecimal getPriceBySkuId(@PathVariable Long skuId) {
        return apiItemService.getPriceBySkuId(skuId);
    }

    /**
     * 根据spuId查询spu所有的销售属性（包括销售属性对应的所有销售属性值），并标记商品的销售属性值
     *
     * @param spuId
     * @return
     */
    @Java0217Cache("getSpuSaleAttrBySpuId:")
    @GetMapping("/getSpuSaleAttrBySpuId/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(@PathVariable Long spuId,
                                                   @PathVariable Long skuId) {
        return apiItemService.getSpuSaleAttrBySpuIdAndSkuId(spuId, skuId);
    }

    /**
     * 查询页面切换所需要的数据
     *
     * @param spuId
     * @return
     */
    @Java0217Cache("getSkuSaleAttrValueBySpuId:")
    @GetMapping("/getSkuSaleAttrValueBySpuId/{spuId}")
    public Map getSkuSaleAttrValueBySpuId(@PathVariable Long spuId) {
        return apiItemService.getSkuSaleAttrValueBySpuId(spuId);
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @RequestMapping("/getBaseTrademarkById/{id}")
    public BaseTrademark getBaseTrademarkById(@PathVariable Long id) {
        return apiItemService.getBaseTrademarkById(id);
    }

    /**
     * 根据skuId查询商品对应的平台属性和平台属性值
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/getBaseAttrInfoBySkuId/{skuId}")
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(@PathVariable Long skuId) {
        return apiItemService.getBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 商品下单时扣减库存
     * @param map
     * @return
     */
    @GetMapping("/decreaseStock")
    public Boolean decreaseStock(@RequestParam Map<String, Object> map) {
        return
                apiItemService.decreaseStock(map);
    }

    /**
     * 回滚库存
     * @param skuParam
     * @return
     */
    @DeleteMapping("/rollbackStock")
    public Boolean rollbackStock(@RequestParam Map<String, Object> skuParam){
        System.out.println("skuParam = " + skuParam);
        return apiItemService.rollbackStock(skuParam);
    }
}
