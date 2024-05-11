package com.hmall.search.service.impl;


import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.hmall.api.client.ItemClient;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.doc.ItemDoc;
import com.hmall.common.domain.dto.ItemDTO;
import com.hmall.common.domain.query.SearchItemQuery;
import com.hmall.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ItemClient itemClient;

    //补全
    @Override
    public List<String> getSuggestion(String key) {
        try {
            //准备请求
            SearchRequest items = new SearchRequest("items");
            //准备DSL
            items.source().suggest(new SuggestBuilder().addSuggestion("suggestion",
                    SuggestBuilders.completionSuggestion
                                    ("suggestion").
                            prefix(key).//关键字
                            skipDuplicates(true)//跳过重复
                            .size(10)));
            //发送请求
            SearchResponse search = client.search(items, RequestOptions.DEFAULT);
            List<String> list = new ArrayList<>();
            //解析结果
            Suggest suggest = search.getSuggest();
            //根据补全查询名称，获取补全结果
            CompletionSuggestion suggestion = suggest.getSuggestion("suggestion");
            //获取options
            List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
            //遍历
            for (CompletionSuggestion.Entry.Option option : options) {
                String text = option.getText().toString();
                list.add(text);
            }
            return list;
        } catch (IOException e) {
            log.error("智能补全失败！", e);
            throw new RuntimeException(e);
        }
    }

    //基本搜索
    @Override
    public PageDTO<ItemDoc> getList(SearchItemQuery searchItemQuery) {
        try {
            //创建请求对象
            SearchRequest items = new SearchRequest("items");
            //准备DSL
            buildBasicQuery(searchItemQuery, items);
            //发送请求
            SearchResponse search = client.search(items, RequestOptions.DEFAULT);
            //解析响应并返回前端需要的对象格式
            return toPageResult(search);
        } catch (IOException e) {
            log.error("基本查询失败！", e);
            throw new RuntimeException(e);
        }
    }

    //过滤
    @Override
    public Map<String, List<String>> getFilters(SearchItemQuery searchItemQuery) {
        try {
            //创建请求对象
            SearchRequest items = new SearchRequest("items");
            //准备DSL
            buildBasicQuery(searchItemQuery, items);
            // 聚合
            items.source().aggregation(AggregationBuilders.terms("brand").field("brand").size(20));
            items.source().aggregation(AggregationBuilders.terms("category").field("category").size(20));
            //发出请求
            SearchResponse search = client.search(items, RequestOptions.DEFAULT);
            //解析结果
            Aggregations aggregations = search.getAggregations();
            //获取品牌结果
            List<String> brand = getlist("brand", aggregations);
            //获取结果
            List<String> category = getlist("category", aggregations);
            Map<String, List<String>> map = new HashMap<>();
            map.put("category", category);
            map.put("brand", brand);
            return map;
        } catch (IOException e) {
            log.error("过滤查询失败！", e);
            throw new RuntimeException(e);
        }
    }

    //添加（查询或新增文档数据）
    @Override
    public void insert(Long id) {
        try {
            ItemDTO itemDTO = itemClient.queryItemById(id);
            IndexRequest items = new IndexRequest("items").id(id.toString());
            items.source(JSONUtil.toJsonStr(itemDTO), XContentType.JSON);
            client.index(items, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //删除（删除文档）
    @Override
    public void delete(Long id) {
        try {
            DeleteRequest items = new DeleteRequest("items").id(id.toString());
            client.delete(items, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<String> getlist(String brand, Aggregations aggregations) {
        List<String> list = new ArrayList<>();
        //根据聚合名称获取聚合结果
        Terms brands = aggregations.get(brand);
        //获取buckets
        List<? extends Terms.Bucket> buckets = brands.getBuckets();
        //遍历
        for (Terms.Bucket bucket : buckets) {
            //获取key
            String keyAsString = bucket.getKeyAsString();
            list.add(keyAsString);
        }
        return list;
    }

    private PageDTO<ItemDoc> toPageResult(SearchResponse response) {
        //创建前端需求对象
        PageDTO<ItemDoc> pageResult = new PageDTO<>();
        //解析响应
        SearchHits searchHits = response.getHits();
        //获得数据总条数
        long value = searchHits.getTotalHits().value;
        pageResult.setTotal(value);
        //获得文档数组
        SearchHit[] hits = searchHits.getHits();
        List<ItemDoc> itemDocs = new ArrayList<>();
        //遍历文档数组
        for (SearchHit hit : hits) {
            //获得文档source的JSON格式
            String json = hit.getSourceAsString();
            //反序列化获得对象
            ItemDoc itemDoc = JSONUtil.toBean(json, ItemDoc.class);
            //获得高亮集合
            Map<String, HighlightField> hlf = hit.getHighlightFields();
            //判断高亮集合是否为空
            if (hlf != null && !hlf.isEmpty()) {
                //从集合中取出name的高亮部分
                HighlightField name = hlf.get("name");
                //判断是否有内容
                if (name != null) {
                    //取出name高亮部分
                    String hlfName = name.getFragments()[0].toString();
                    //   System.out.println(s);
                    //高亮name代替原本内容
                    itemDoc.setName(hlfName);
                }
            }
            //将对象放入list集合
            itemDocs.add(itemDoc);
        }
        //放入前端需要的对象中并返回
        pageResult.setList(itemDocs);
        return pageResult;
    }

    private void buildBasicQuery(SearchItemQuery searchItemQuery, SearchRequest request) {
        //创建bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String key = searchItemQuery.getKey();
        //判断key及搜索框是否有输入内容
        if (StringUtils.isNotBlank(key)) {
            //key不为空，分词搜索all中等于key
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", key));
        } else {
            //key为空，搜索全部
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        String category = searchItemQuery.getCategory();
        //判断city是否为空及页面是否选择了城市
        if (StringUtils.isNotBlank(category)) {
            //过滤精准查询category字段和category相等
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));
        }
        String brand = searchItemQuery.getBrand();
        //判断brand是否为空及页面是否选择了品牌
        if (StringUtils.isNotBlank(brand)) {
            //过滤 精准查询brand字段为brand
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", brand));
        }

        //判断价格范围的前后值是否为空
        if (searchItemQuery.getMinPrice() != null && searchItemQuery.getMaxPrice() != null) {
            //过滤 价格字段在范围内中的
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .gte(searchItemQuery.getMinPrice() * 100)
                    .lte(searchItemQuery.getMaxPrice() * 100));
        }

        //算分查询  判断isAD是否为true，为true及进行加分 没有设置方法就是相乘 乘以10  当有其他排序的时候，得分算法就会失效，
        //并将bool查询封装到算分查询对象中
        FunctionScoreQueryBuilder isAD = QueryBuilders.functionScoreQuery(
                boolQueryBuilder, new FunctionScoreQueryBuilder.FilterFunctionBuilder[]
                        {new FunctionScoreQueryBuilder.FilterFunctionBuilder
                                (QueryBuilders.termQuery("isAD", true),
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )});

        //  request.source().query(boolQueryBuilder);
        //将算分DSL封装到查询请求中
        request.source().query(isAD);

        //设置高亮 将搜索key中的分词在name字段中出现的设置高亮
        if (StringUtils.isNotBlank(searchItemQuery.getKey())) {
            request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(Boolean.FALSE).
                    preTags("<em>").postTags("</em>"));
        }
        //判断页码是否为空
        int page = 1;
        if (searchItemQuery.getPageNo() != null) {
            page = searchItemQuery.getPageNo();
        }
        //判断每页行数是否为空
        int size = 10;
        if (searchItemQuery.getPageSize() != null) {
            size = searchItemQuery.getPageSize();
        }
        //设置分页
        request.source().from((page - 1) * size).size(size);
        //判断是否按照价钱排序
        if (searchItemQuery.getSortBy().equals("price")) {
            request.source().sort("price", SortOrder.ASC).sort("sold", SortOrder.DESC);
        }
        //判断是否按照评分排序
        if (searchItemQuery.getSortBy().equals("sold")) {
            request.source().sort("sold", SortOrder.DESC).sort("price", SortOrder.ASC);
        }
//            hotel.source().sort("price",SortOrder.ASC);
    }

}

