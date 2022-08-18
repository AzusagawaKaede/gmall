package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: feign使用的service
 */
public interface ApiItemService {

    /**
     * 根据skuId查询skuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuId(Long skuId);

    /**
     * 根据skuId从Redis中查询skuInfo
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuIdFromRedis(Long skuId);

    /**
     * 根据skuId从Redis中查询skuInfo
     * 改进
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfoBySkuIdFromRedis2(Long skuId);

    /**
     * 根据三级分类id查询对应的一级分类、二级分类
     * @param category3Id
     * @return
     */
    BaseCategoryView getBaseCategoryViewById(Long category3Id);

    /**
     * 根据skuId查询对应的图片列表
     * @param skuId
     * @return
     */
    List<SkuImage> getSkuImageListBySkuId(Long skuId);

    /**
     * 根据skuId查询价格
     * @param skuId
     * @return
     */
    BigDecimal getPriceBySkuId(Long skuId);

    /**
     * 根据spuId查询spu所有的销售属性（包括销售属性对应的所有销售属性值），并标记商品的销售属性值
     * @param spuId
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrBySpuIdAndSkuId(Long spuId, Long skuId);

    /**
     * 查询页面切换所需要的数据
     * @param spuId
     * @return
     */
    Map getSkuSaleAttrValueBySpuId(Long spuId);

    /**
     * 根据id查询品牌信息
     * @param id
     * @return
     */
    BaseTrademark getBaseTrademarkById(Long id);

    /**
     * 根据skuId获取商品的平台属性
     * 虽然每个商品的平台属性有多个，但是每个商品的平台属性的值只有一个
     * 所以每个BaseAttrInfo的List<BaseAttrValue>有且仅有一个
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoBySkuId(Long skuId);

    /**
     * 用户购买商品时，删除库存
     * @param map
     * @return
     */
    Boolean decreaseStock(Map<String, Object> map);

    /**
     * 回滚库存
     * @param skuParam
     * @return
     */
    Boolean rollbackStock(Map<String, Object> skuParam);
}
