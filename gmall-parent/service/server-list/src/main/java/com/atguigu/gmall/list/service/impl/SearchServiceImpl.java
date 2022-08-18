package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchResponseAttrVo;
import com.atguigu.gmall.model.list.SearchResponseTmVo;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: rlk
 * @date: 2022/8/6
 * Description:
 */
@Service
public class SearchServiceImpl implements SearchService {

    /**
     * 注入Es客户端，SpringBoot集成Es后使用的是RestHighLevelClient
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 根据关键字和选中的平台属性查询商品
     * 参数约定：URL?keywords=关键字&tradeMark=1:华为&attr_平台属性name=id:value&price=价格
     *
     * @param searchData
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchData) {
        try {
            //拼接查询条件
            SearchRequest searchRequest = buildSearchParams(searchData);

            //查询Es，searchRequest是上一步拼接好的查询条件，RequestOptions.DEFAULT是请求的其他配置，这里使用默认的
            SearchResponse resp = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //解析查询获得的结果
            return solveData(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回结果
        return null;
    }

    /**
     * 拼接查询条件
     *
     * @param searchData
     * @return
     */
    private SearchRequest buildSearchParams(Map<String, String> searchData) {
        //参数校验，校验Map集合判断是否为null，以及Size
        if (searchData == null || searchData.size() == 0) {
            return null;
        }

        //创建searchSourceBuilder用来拼接查询条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //前端还要传入平台属性，因此需要使用组合查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //根据关键字查询，可以分词的查询有：匹配查询和字符串查询。这里需要对指定域查询，所以使用匹配查询
        String keywords = searchData.get("keywords");
        if (!StringUtils.isEmpty(keywords)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keywords));
        }

        //对品牌进行聚合，相当添加group by tm_id子句
        //field设置被聚合的域，terms设置别名，必须设置，后续取值通过别名来取值
        builder.aggregation(
                //先聚合tmId
                AggregationBuilders.terms("aggTmId").field("tmId")
                        //聚合tmName
                        .subAggregation(AggregationBuilders.terms("aggTmName").field("tmName"))
                        .subAggregation(AggregationBuilders.terms("aggTmLogoUrl").field("tmLogoUrl"))
        );

        //对平台属性进行聚合 -- 一共三层，比品牌聚合多一层
        builder.aggregation(
                //对Goods的attrs属性进行分组，attrs属性的类型是FieldType.Nested，所以使用AggregationBuilders.nested
                //第一个参数是别名，第二个参数是Goods中的属性名 -- attrs属性
                AggregationBuilders.nested("aggAttrs", "attrs")
                        //先聚合attrId
                        .subAggregation(AggregationBuilders.terms("aggAttrId").field("attrs.attrId")
                                //再聚合attrName
                                .subAggregation(AggregationBuilders.terms("aggAttrName").field("attrs.attrName"))
                                //最后再聚合attrValue
                                .subAggregation(AggregationBuilders.terms("aggAttrValue").field("attrs.attrValue")).size(100)
                        )
        );

        //品牌条件查询
        String tradeMark = searchData.get("tradeMark");
        if (!StringUtils.isEmpty(tradeMark)) {
            //说明传入了tradeMark，检验参数是否正确
            String[] split = tradeMark.split(":");
            if (split.length == 2) {
                Long tmId = Long.valueOf(split[0]);
                boolQueryBuilder.must(QueryBuilders.termQuery("tmId", tmId));
            }
        }

        //平台属性条件查询
        searchData.entrySet().stream().forEach(entry -> {
            //遍历前端传入的查询条件，key以attr_开头的就是平台属性
            String key = entry.getKey();
            if (key.startsWith("attr_")) {
                //说明是平台属性，组合查询判断平台属性的id和平台属性的value相等
                String value = entry.getValue();
                String[] split = value.split(":");
                //再创建一个boolQuery
                BoolQueryBuilder nestBoolQueryBuilder = QueryBuilders.boolQuery();
                //添加判断id和value
                nestBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                nestBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                //这里attrs是nested类型的，要使用nestedQuery，第一个参数是属性名称，第二个参数是QueryBuilder，第三个参数是模式
                //这里要判断id和value，所以还需要一个组合查询
                boolQueryBuilder.must(QueryBuilders.nestedQuery("attrs", nestBoolQueryBuilder, ScoreMode.None));
            }
        });

        //价格查询
        String price = searchData.get("price");
        if (!StringUtils.isEmpty(price)) {
            //说明有价格：约定格式：price=0-500  price=3000-
            String[] split = price.split("-");
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            //大于
            rangeQueryBuilder.gte(split[0]);
            //小于
            if (split.length == 2 && split[1] != "") {
                //说明有上限值
                rangeQueryBuilder.lt(split[1]);
            }
            boolQueryBuilder.must(rangeQueryBuilder);
        }

        //设置排序：sortField=值&sortRule=值
        String sortField = searchData.get("sortField");
        String sortRule = searchData.get("sortRule");
        if (!StringUtils.isEmpty(sortField)) {
            //设置了排序字段
            if (!StringUtils.isEmpty(sortRule)) {
                //设置了升序还是降序
                if ("ASC".equalsIgnoreCase(sortRule)) {
                    //升序
                    builder.sort(sortField, SortOrder.ASC);
                } else if ("DESC".equalsIgnoreCase(sortRule)) {
                    //降序
                    builder.sort(sortField, SortOrder.DESC);
                } else {
                    //非法参数
                    builder.sort(sortField, SortOrder.DESC);
                }
            } else {
                //没有设置升序还是降序，默认降序
                builder.sort(sortField, SortOrder.DESC);
            }
        } else {
            //没有设置排序字段，那就默认排序，id降序
            builder.sort("id", SortOrder.DESC);
        }

        //分页，设置每次查询50条数据，前端参数：pageNim=值
        builder.size(50);
        String pageNum = searchData.get("pageNum");
        if (!StringUtils.isEmpty(pageNum)) {
            //前端传入了pageNum，但是需要校验参数
            builder.from(getPage(pageNum));
        } else {
            //没有传入参数，默认展示第一页，下标从0开始
            builder.from(0);
        }

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置高亮的域
        highlightBuilder.field("title");
        //设置前缀和后缀
        highlightBuilder.preTags("<font style='color:red'>");
        highlightBuilder.postTags("</font>");
        builder.highlighter(highlightBuilder);

        //设置查询的索引名称和添加查询条件
        SearchRequest searchRequest = new SearchRequest();
        builder.query(boolQueryBuilder);
        searchRequest.indices("goods_java0107").source(builder);
        //返回
        return searchRequest;
    }

    /**
     * 前端传入pageNum，计算从哪一行还是
     *
     * @param pageNum
     * @return
     */
    private int getPage(String pageNum) {
        try {
            int from = Integer.parseInt(pageNum);
            if (from <= 0) {
                return 0;
            } else {
                return (from - 1) * 50;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 解析查询获得的结果
     *
     * @param resp
     * @return
     */
    private Map<String, Object> solveData(SearchResponse resp) {
        //返回多个结果，使用Map封装
        Map<String, Object> result = new HashMap<>();


        //获取命中关键字的Goods
        SearchHits hits = resp.getHits();
        //获取命中的总条数
        long totalHits = hits.getTotalHits();
        result.put("totalHits", totalHits);
        //创建集合保存Goods
        List<Goods> goodsList = new ArrayList<>();
        //遍历查询出来的文档
        Iterator<SearchHit> it = hits.iterator();
        while (it.hasNext()) {
            //每一行数据
            SearchHit next = it.next();
            //转为字符串
            String sourceAsString = next.getSourceAsString();
            //反序列化
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            //解析title里的高亮数据
            HighlightField title = next.getHighlightFields().get("title");
            if (title != null) {
                Text[] fragments = title.getFragments();
                if(fragments != null && fragments.length > 0){
                    //拼接高亮的数据
                    StringBuffer sb = new StringBuffer();
                    for (Text fragment : fragments) {
                        sb.append(fragment);
                    }
                    //替换
                    goods.setTitle(sb.toString());
                }
            }
            goodsList.add(goods);
        }
        //查询出来的Goods列表保存到result
        result.put("goodsList", goodsList);

        //解析品牌聚合数据
        List<SearchResponseTmVo> searchResponseTmVoList = solveTmAggData(resp);
        //保存到result
        result.put("searchResponseTmVoList", searchResponseTmVoList);

        //解析平台属性聚合数据
        List<SearchResponseAttrVo> searchResponseAttrVoList = solveAttrsAggData(resp);
        result.put("searchResponseAttrVoList", searchResponseAttrVoList);

        //返回解析结果result
        return result;
    }

    /**
     * 解析平台属性的聚合数据
     *
     * @param resp
     * @return
     */
    private List<SearchResponseAttrVo> solveAttrsAggData(SearchResponse resp) {
        //解析平台属性的聚合数据
        Aggregations aggregations = resp.getAggregations();
        //获取attrs
        ParsedNested aggAttrs = aggregations.get("aggAttrs");
        //获取attrId列表
        ParsedLongTerms aggAttrId = aggAttrs.getAggregations().get("aggAttrId");
        //遍历attrId列表
        return aggAttrId.getBuckets().stream().map(aggAttrIdBuckets -> {
            //创建一个对象接收
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取attrId
            long attrId = ((Terms.Bucket) aggAttrIdBuckets).getKeyAsNumber().longValue();
            searchResponseAttrVo.setAttrId(attrId);
            //获取attrName，每个attrName都一样，直接取第一个
            ParsedStringTerms aggAttrName = ((Terms.Bucket) aggAttrIdBuckets).getAggregations().get("aggAttrName");
            //非空校验，防止空指针异常
            if (!aggAttrName.getBuckets().isEmpty()) {
                //设置attrName
                String attrName = aggAttrName.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
            }
            //获取属性值列表，创建一个集合存储属性值
            ParsedStringTerms aggAttrValue = ((Terms.Bucket) aggAttrIdBuckets).getAggregations().get("aggAttrValue");
            //非空校验
            if (!aggAttrValue.getBuckets().isEmpty()) {
                //遍历存储到集合中
                List<String> attrValueList = aggAttrValue.getBuckets().stream().map(aggAttrValueBuckets -> {
                    return ((Terms.Bucket) aggAttrValueBuckets).getKeyAsString();
                }).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(attrValueList);
            }
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
    }

    /**
     * 解析品牌聚合的数据
     *
     * @param resp
     * @return
     */
    private List<SearchResponseTmVo> solveTmAggData(SearchResponse resp) {
        //解析聚合的品牌
        Aggregations aggregations = resp.getAggregations();
        //获取tmId列表 -- 品牌Id在数据库和Es中是long类型的，所以使用的是parsedLongTerms
        ParsedLongTerms aggTmId = aggregations.get("aggTmId");
        //遍历集合，获取每一个tmId
        return aggTmId.getBuckets().stream().map(aggTmIdBucket -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取tmId
            long tmId = ((Terms.Bucket) aggTmIdBucket).getKeyAsNumber().longValue();
            searchResponseTmVo.setTmId(tmId);
            //获取tmName列表 -- tmName和tmLogUrl是字符串类型的所以使用PaesedStringTerms
            ParsedStringTerms aggTmName = ((Terms.Bucket) aggTmIdBucket).getAggregations().get("aggTmName");
            //判断tmName列表非空，获取第一个tmName即该品牌的名称，tmName和logUrl是String类型的 -- getKeyAsString
            if (!aggTmName.getBuckets().isEmpty()) {
                //获取tmName
                String tmName = aggTmName.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
            }
            //获取tmLogUrl列表
            ParsedStringTerms aggTmLogoUrl = ((Terms.Bucket) aggTmIdBucket).getAggregations().get("aggTmLogoUrl");
            //判断tmLogUrl列表非空，获取第一个tmLogUrl即该品牌的logUrl
            if (!aggTmLogoUrl.getBuckets().isEmpty()) {
                String tmLogUrl = aggTmLogoUrl.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogUrl);
            }
            return searchResponseTmVo;
        }).collect(Collectors.toList());
    }
}
