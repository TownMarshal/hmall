package com.hmall.search.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmall.api.client.ItemClient;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.R;
import com.hmall.common.domain.doc.ItemDoc;
import com.hmall.common.domain.dto.ItemDTO;
import com.hmall.common.domain.po.Item;
import com.hmall.common.domain.query.ItemPageQuery;
import com.hmall.common.domain.query.SearchItemQuery;
import com.hmall.common.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.hmall.search.service.SearchService;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Autowired
    private SearchService searchService;

    //基本搜索 TODO 参考ElasticSearchTest做修改，带测试
    @PostMapping("/list")
    public PageDTO<ItemDoc> getList(@RequestBody SearchItemQuery dto) {
        return searchService.getList(dto);
    }

    //补全
    @GetMapping("/suggestion")
    public List<String> getSuggestion(@RequestParam("key") String key) {
        return searchService.getSuggestion(key);

    }

    //过滤  TODO 参考ElasticSearchTest做修改，待测试
    @PostMapping("/filters")
    public Map<String, List<String>> getFilters(@RequestBody SearchItemQuery dto) {
        return searchService.getFilters(dto);
    }

    //TODO 批量导入文档

}