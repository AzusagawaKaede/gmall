package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/4
 * Description:
 */
@FeignClient(name = "service-product", path = "/api/index", contextId = "service-product-index")
public interface IndexFeignService {
    /**
     * 查询首页所有的分类信息
     * @return
     */
    @RequestMapping("/getCategoryList")
    public List<JSONObject> getCategoryList();
}
