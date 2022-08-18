package com.atguigu.gmall.list.service;

import java.util.Map;

/**
 * @author: rlk
 * @date: 2022/8/6
 * Description: 搜索的业务层
 */
public interface SearchService {

    /**
     * 根据关键字查询商品，对应搜索框
     * @param searchData
     * @return
     * @throws Exception
     */
    public Map<String, Object> search(Map<String, String> searchData) ;
}
