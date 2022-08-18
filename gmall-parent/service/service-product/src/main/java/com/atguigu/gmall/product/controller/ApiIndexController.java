package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.service.ApiIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/4
 * Description:
 */
@RestController
@RequestMapping("/api/index")
public class ApiIndexController {

    @Autowired
    private ApiIndexService apiIndexService;

    /**
     * 查询首页所有的分类信息
     * @return
     */
    @RequestMapping("/getCategoryList")
    public List<JSONObject> getCategoryList(){
        return apiIndexService.getCategoryList();
    }
}
