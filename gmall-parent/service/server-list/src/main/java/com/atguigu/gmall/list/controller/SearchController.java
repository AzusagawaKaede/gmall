package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/6
 * Description: 搜索页面的远程api
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索页面的条件查询
     * @param searchData
     * @return
     * @throws Exception
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam Map<String, String> searchData){
        return searchService.search(searchData);
    }
}
