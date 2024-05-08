package com.hmall.search.service.impl;


import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.hmall.api.client.ItemClient;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.doc.ItemDoc;
import com.hmall.common.domain.dto.ItemDTO;
import com.hmall.common.domain.query.SearchItemQuery;
import com.hmall.search.service.SearchService;
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
            SearchRequest hotel = new SearchRequest("item");
            //准备DSL
            hotel.source().suggest(new SuggestBuilder().addSuggestion("suggestion",
                    SuggestBuilders.completionSuggestion
                                    ("suggestion").
                            prefix(key).//关键字
                            skipDuplicates(true)//跳过重复
                            .size(10)));
            //发送请求
            SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //基本搜索
    @Override
    public PageDTO<ItemDoc> getList(SearchItemQuery dto) {
        try {
            //创建请求对象
            SearchRequest hotel = new SearchRequest("item");
            //准备DSL
            buildBasicQuery(dto, hotel);
            //发送请求
            SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
            //解析响应并返回前端需要的对象格式
            return toPageResult(search);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //过滤
    @Override
    public Map<String, List<String>> getFilters(SearchItemQuery dto) {
        try {
            //创建请求对象
            SearchRequest hotel = new SearchRequest("item");
            //准备DSL
            buildBasicQuery(dto, hotel);
            // 聚合
            hotel.source().aggregation(AggregationBuilders.terms("brand").field("brand").size(20));
            hotel.source().aggregation(AggregationBuilders.terms("category").field("category").size(20));
            //发出请求
            SearchResponse search = client.search(hotel, RequestOptions.DEFAULT);
            //解析结果
            Aggregations aggregations = search.getAggregations();
            //获取品牌结果
            List<String> brand = getlist("brand", aggregations);
            //获取结果
            List<String> city = getlist("category", aggregations);
            Map<String, List<String>> map = new HashMap<>();
            map.put("category", city);
            map.put("brand", brand);
            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //添加
    @Override
    public void insert(Long id) {
        try {
            ItemDTO itemDTO = itemClient.queryItemById(id);
            IndexRequest hotel = new IndexRequest("item").id(id.toString());
            hotel.source(JSONUtil.toJsonStr(itemDTO), XContentType.JSON);
            client.index(hotel, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    //删除
    @Override
    public void delete(Long id) {
        try {
            DeleteRequest hotel = new DeleteRequest("item").id(id.toString());
            client.delete(hotel, RequestOptions.DEFAULT);
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

    private PageDTO<ItemDoc> toPageResult(SearchResponse search) {
        //创建前端需求对象
        PageDTO<ItemDoc> pageResult = new PageDTO<>();
        //解析响应
        SearchHits hits = search.getHits();
        //获得数据总条数
        long value = hits.getTotalHits().value;
        pageResult.setTotal(value);
        //获得文档数组
        SearchHit[] hits1 = hits.getHits();
        List<ItemDoc> hotelDocs = new ArrayList<>();
        //遍历文档数组
        for (SearchHit documentFields : hits1) {
            //获得文档source的JSON格式
            String json = documentFields.getSourceAsString();
            //反序列化获得对象
            ItemDoc hotelDoc = JSONUtil.toBean(json, ItemDoc.class);
            //获得高亮集合
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            //  System.out.println(highlightFields);
            //判断高亮集合是否为空
            if (highlightFields != null && !highlightFields.isEmpty()) {
                //从集合中取出name的高亮部分
                HighlightField name = highlightFields.get("name");
                //判断是否有内容
                if (name != null) {
                    //取出name高亮部分
                    String s = name.getFragments()[0].toString();
                    //   System.out.println(s);
                    //高亮name代替原本内容
                    hotelDoc.setName(s);
                }
            }
            //将对象放入list集合
            hotelDocs.add(hotelDoc);
        }
        //放入前端需要的对象中并返回
        pageResult.setList(hotelDocs);
        return pageResult;
    }

    private void buildBasicQuery(SearchItemQuery dto, SearchRequest request) {
        //创建bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        String key = dto.getKey();
        //判断key及搜索框是否有输入内容
        if (StringUtils.isNotBlank(key)) {
            //key不为空，分词搜索all中等于key
            boolQueryBuilder.must(QueryBuilders.matchQuery("all", key));
        } else {
            //key为空，搜索全部
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        String city = dto.getCategory();
        //判断city是否为空及页面是否选择了城市
        if (StringUtils.isNotBlank(city)) {
            //过滤精准查询city字段和city相等
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", city));
        }
        String brand = dto.getBrand();
        //判断brand是否为空及页面是否选择了品牌
        if (StringUtils.isNotBlank(brand)) {
            //过滤 精准查询brand字段为brand
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand", brand));
        }

        //判断价格范围的前后值是否为空
        if (dto.getMinPrice() != null && dto.getMaxPrice() != null) {
            //过滤 价格字段在范围内中的
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .gte(dto.getMinPrice() * 100)
                    .lte(dto.getMaxPrice() * 100));
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
        if (StringUtils.isNotBlank(dto.getKey())) {
            request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(Boolean.FALSE).
                    preTags("<em>").postTags("</em>"));
        }
        //判断页码是否为空
        int page = 1;
        if (dto.getPageNo() != null) {
            page = dto.getPageNo();
        }
        //判断每页行数是否为空
        int size = 10;
        if (dto.getPageSize() != null) {
            size = dto.getPageSize();
        }
        //设置分页
        request.source().from((page - 1) * size).size(size);
        //判断是否按照价钱排序
        if (dto.getSortBy().equals("price")) {
            request.source().sort("price", SortOrder.ASC).sort("sold", SortOrder.DESC);
        }
        //判断是否按照评分排序
        if (dto.getSortBy().equals("sold")) {
            request.source().sort("sold", SortOrder.DESC).sort("price", SortOrder.ASC);
        }
//            hotel.source().sort("price",SortOrder.ASC);
    }

}

