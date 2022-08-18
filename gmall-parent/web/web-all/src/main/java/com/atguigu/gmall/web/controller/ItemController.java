package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/3
 * Description: 商品详情页的控制层
 */
@Controller
@RequestMapping("/page/item")
public class ItemController {

    @Value("${item.base-path}")
    private String basePath;

    @Resource
    private ItemFeignService itemFeignService;
    @Resource
    private TemplateEngine templateEngine;


    @GetMapping("/{skuId}")
    public String item(Model model, @PathVariable Long skuId){
        Map map = itemFeignService.getItemInfo(skuId);
        model.addAllAttributes(map);
        return "item";
    }

    /**
     * 根据skuId生成静态页面
     * @param skuId
     * @return
     */
    @GetMapping("/createHtml/{skuId}")
    @ResponseBody
    public Result test(@PathVariable Long skuId) throws Exception {
        //获取数据
        Map map = itemFeignService.getItemInfo(skuId);
        //准备模板引擎需要的容器
        Context context = new Context();
        //向容器中存放数据
        context.setVariables(map);
        //创建文件输出流
        PrintWriter writer = new PrintWriter(basePath + skuId + ".html");
        //模板引擎执行，通过流生成静态页面到本地
        templateEngine.process("item_pro", context, writer);
        //返回成功
        return Result.ok();
    }
}
