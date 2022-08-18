package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: 商品详情页的控制层
 */
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * 根据skuId查询商品详情页信息
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map getItemInfo(@PathVariable Long skuId){
        return itemService.getItemInfo(skuId);
    }

}
