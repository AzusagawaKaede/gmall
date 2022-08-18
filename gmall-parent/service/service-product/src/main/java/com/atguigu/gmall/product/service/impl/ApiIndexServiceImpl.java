package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.ApiIndexService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: rlk
 * @date: 2022/8/4
 * Description:
 */
@Service
public class ApiIndexServiceImpl implements ApiIndexService {

    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;

    /**
     * 获取首页的分类列表
     *
     * @return
     */
    @Override
    public List getCategoryList() {
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //按一级分类分组
        Map<Long, List<BaseCategoryView>> category1List =
                baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //遍历分组后的结果
        List<JSONObject> categoryList = category1List.entrySet().stream().map(category1 -> {
            //所有的一级分类
            List<BaseCategoryView> category1Values = category1.getValue();
            //保存一级分类
            JSONObject category1Json = new JSONObject();
            //一级分类Id
            category1Json.put("categoryId", category1Values.get(0).getCategory1Id());
            //一级分类名称
            category1Json.put("categoryName", category1Values.get(0).getCategory1Name());
            //一级分类所有的子分类
            Map<Long, List<BaseCategoryView>> category2List =
                    category1Values.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //遍历二级分类
            List<JSONObject> childCategoryList2 = category2List.entrySet().stream().map(category2 -> {
                //当前一级分类下所有的二级分类
                List<BaseCategoryView> category2Values = category2.getValue();
                //保存二级分类
                JSONObject category2Json = new JSONObject();
                //二级分类Id
                category2Json.put("categoryId", category2Values.get(0).getCategory2Id());
                //二级分类名称
                category2Json.put("categoryName", category2Values.get(0).getCategory2Name());
                //二级分类所有的子分类
                Map<Long, List<BaseCategoryView>> category3List =
                        category2Values.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                //遍历三级分类
                List<JSONObject> childCategoryList3 = category3List.entrySet().stream().map(category3 -> {
                    //当前二级分类下所有的三级分类
                    List<BaseCategoryView> category3Values = category3.getValue();
                    JSONObject category3Json = new JSONObject();
                    category3Json.put("categoryId", category3Values.get(0).getCategory3Id());
                    category3Json.put("categoryName", category3Values.get(0).getCategory3Name());
                    return category3Json;
                }).collect(Collectors.toList());
                //保存三级分类
                category2Json.put("childCategoryList", childCategoryList3);
                return category2Json;
            }).collect(Collectors.toList());
            //保存二级分类
            category1Json.put("childCategoryList", childCategoryList2);
            return category1Json;
        }).collect(Collectors.toList());
        return categoryList;
    }
}
