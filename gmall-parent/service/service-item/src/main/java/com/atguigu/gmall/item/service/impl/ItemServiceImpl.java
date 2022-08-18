package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: 商品详情页的业务层
 * 注意：service-item模块本身不对数据库进行任何操作，全部通过OpenFeign调用service-product获取数据
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 获取商品详情页信息
     * 2022/8/3：通过异步编排并行化feign，加快查询速度
     *
     * @param skuId
     * @return
     */
    @Override
    public Map getItemInfo(Long skuId) {
        //校验参数，判断skuId是否为null
        if (skuId == null) {
            throw new RuntimeException("商品不存在");
        }

//        HashMap<Object, Object> map = new HashMap<>();
        //这里异步编排，涉及到多个线程执行写操作，不可以使用HashMap！
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>();

        //第一步：查询skuInfo，这里后续同步查询需要用到skuInfo，所以使用supplyAsync，可以获取到任务的返回值
        CompletableFuture<SkuInfo> future = CompletableFuture.supplyAsync(() -> {
            //查询skuId对应的商品是否存在 -- 根据skuId查询sku_info表
            SkuInfo skuInfo = productFeignService.getSkuInfoBySkuId(skuId);
            if (skuInfo == null || skuInfo.getId() == null) {
                //使用了异步编排，这里就不要通过抛异常的方式返回，直接return
                return null;
            }
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        //第二步：同时查询baseCategoryView、skuImage、price、spuSaleAttr、skuSaleAttrValue
        //这里需要获取第一步任务的返回值skuInfo，由于直接在任务里写入map，因此任务不需要返回值，所以使用thenAccept
        CompletableFuture<Void> future1 = future.thenAccept(skuInfo -> {
            //查询商品的一级、二级、三级分类id和名称 -- 根据三级分类id查询base_category_view视图 -- 三级分类id从sku_info获取
            BaseCategoryView baseCategoryView = productFeignService.getBaseCategoryViewById(skuInfo.getCategory3Id());
            map.put("baseCategoryView", baseCategoryView);
        });

        CompletableFuture<Void> future2 = future.thenAccept(skuInfo -> {
            //查询商品对应的图片列表 -- 根据skuId查询sku_image表
            List skuImageList = productFeignService.getSkuImageListBySkuId(skuInfo.getId());
            map.put("imageList", skuImageList);
        });

        CompletableFuture<Void> future3 = future.thenAccept(skuInfo -> {
            //单独查询商品的价格 -- 根据skuId查询sku_info表
            BigDecimal price = productFeignService.getPriceBySkuId(skuInfo.getId());
            map.put("price", price);
        });

        CompletableFuture<Void> future4 = future.thenAccept(skuInfo -> {
            //查询商品的销售属性和销售属性对应的销售属性值，用于页面显示销售属性及其选项
            //商品销售属性和销售属性值由spu决定 -- 根据skuId查询商品的spuId -- 根据spuId查询spu_sale_attr和spu_sale_attr_value
            List<SpuSaleAttr> spuSaleAttrList = productFeignService.getSpuSaleAttrBySpuId(skuInfo.getSpuId(), skuInfo.getId());
            map.put("spuSaleAttrList", spuSaleAttrList);
        });

        CompletableFuture<Void> future5 = future.thenAccept(skuInfo -> {
            //查询商品对应的销售属性值，用于用户切换销售属性值时页面的跳转
            //商品选中的销售属性值 -- 查询sku_sale_attr_value
            Map skuSaleAttrValue = productFeignService.getSkuSaleAttrValueBySpuId(skuInfo.getSpuId());
            map.put("keyAndValues", skuSaleAttrValue);
        });

        //等待所有任务完成，将Map返回
        CompletableFuture.allOf(future, future1, future2, future3, future4, future5).join();
        return map;
    }
}
