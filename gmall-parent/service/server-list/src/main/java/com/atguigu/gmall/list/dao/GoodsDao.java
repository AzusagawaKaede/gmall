package com.atguigu.gmall.list.dao;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author: rlk
 * @date: 2022/8/5
 * Description:
 */
@Repository
public interface GoodsDao extends ElasticsearchRepository<Goods, Long> {
}
