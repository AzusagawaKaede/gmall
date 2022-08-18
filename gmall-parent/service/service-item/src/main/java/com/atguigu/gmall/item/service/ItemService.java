package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/1
 * Description: 商品详情页的业务层
 */
public interface ItemService {

    /**
     * 获取商品详情页信息
     * @param skuId
     * @return
     */
    Map getItemInfo(Long skuId);
}
