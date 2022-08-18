package com.atguigu.gmall.list.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author: rlk
 * @date: 2022/8/5
 * Description:
 */
@FeignClient(name = "service-list", path = "/api/list", contextId = "service-list-list")
public interface ListFeignService {

    /**
     * 根据skuId将商品插入到Es
     * @param skuId
     * @return
     */
    @RequestMapping("/addGoodsToEs/{skuId}")
    public String addGoodsToEs(@PathVariable Long skuId);

    /**
     * 根据id从Es删除商品
     * @param goodsId
     * @return
     */
    @RequestMapping("/deleteGoods/{goodsId}")
    public String deleteGoods(@PathVariable Long goodsId);
}
