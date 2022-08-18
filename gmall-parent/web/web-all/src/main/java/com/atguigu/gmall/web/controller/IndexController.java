package com.atguigu.gmall.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.product.client.IndexFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author: rlk
 * @date: 2022/8/4
 * Description: 首页的控制层
 */
@Controller
@RequestMapping("/page/index")
public class IndexController {

    @Autowired
    private IndexFeignService indexFeignService;

    @RequestMapping
    public String index(Model model){
        List<JSONObject> categoryList = indexFeignService.getCategoryList();
        model.addAttribute("categoryList", categoryList);
        return "index";
    }
}
