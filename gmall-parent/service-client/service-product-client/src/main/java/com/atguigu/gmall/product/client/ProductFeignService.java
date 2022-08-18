package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: service-product的feign接口
 */
@FeignClient(name = "service-product", path = "/api/item")
public interface ProductFeignService {

    /**
     * 根据skuId查询skuInfo
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfoBySkuId/{skuId}")
    public SkuInfo getSkuInfoBySkuId(@PathVariable Long skuId);

    /**
     * 根据三级分类id查询对应的一级分类、二级分类
     *
     * @param category3Id
     * @return
     */
    @GetMapping("/getBaseCategoryViewById/{category3Id}")
    public BaseCategoryView getBaseCategoryViewById(@PathVariable Long category3Id);

    /**
     * 根据skuId查询对应的图片列表
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuImageListBySkuId/{skuId}")
    public List getSkuImageListBySkuId(@PathVariable Long skuId);

    /**
     * 根据skuId查询价格
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getPriceBySkuId/{skuId}")
    public BigDecimal getPriceBySkuId(@PathVariable Long skuId);

    /**
     * 根据spuId查询spu所有的销售属性（包括销售属性对应的所有销售属性值），并标记商品的销售属性值
     *
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttrBySpuId/{spuId}/{skuId}")
    public List<SpuSaleAttr> getSpuSaleAttrBySpuId(@PathVariable Long spuId,
                                                   @PathVariable Long skuId);

    /**
     * 查询页面切换所需要的数据
     *
     * @param spuId
     * @return
     */
    @GetMapping("/getSkuSaleAttrValueBySpuId/{spuId}")
    public Map getSkuSaleAttrValueBySpuId(@PathVariable Long spuId);

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @RequestMapping("/getBaseTrademarkById/{id}")
    public BaseTrademark getBaseTrademarkById(@PathVariable Long id);

    /**
     * 根据skuId查询商品对应的平台属性和平台属性值
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/getBaseAttrInfoBySkuId/{skuId}")
    public List<BaseAttrInfo> getBaseAttrInfoBySkuId(@PathVariable Long skuId);

    /**
     * 商品下单时扣减库存
     * @param map
     * @return
     */
    @GetMapping("/decreaseStock")
    public Boolean decreaseStock(@RequestParam Map<String, Object> map);

    /**
     * 回滚库存
     * @param skuParam
     * @return
     */
    @DeleteMapping("/rollbackStock")
    public Boolean rollbackStock(@RequestParam Map<String, Object> skuParam);
}
