package com.atguigu.gmall.list.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/7
 * Description: 查询的Feign接口
 */
@FeignClient(name = "service-list", path = "/api/search", contextId = "service-list-search")
public interface SearchFeignService {

    /**
     * 搜索页面的条件查询
     *
     * @param searchData
     * @return
     * @throws Exception
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam Map<String, String> searchData) throws Exception;

}