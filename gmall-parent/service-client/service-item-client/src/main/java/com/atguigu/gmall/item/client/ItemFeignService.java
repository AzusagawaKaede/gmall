package com.atguigu.gmall.item.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/3
 * Description:
 */
@FeignClient(value = "service-item", path = "/item")
public interface ItemFeignService {

    /**
     * 根据skuId查询商品详情页信息
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map getItemInfo(@PathVariable Long skuId);
}
