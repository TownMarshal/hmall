package com.hmall.search.service;



import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.doc.ItemDoc;
import com.hmall.common.domain.query.SearchItemQuery;

import java.util.List;
import java.util.Map;

public interface SearchService {
    List<String> getSuggestion(String key);
    PageDTO<ItemDoc> getList(SearchItemQuery dto);
    Map<String, List<String>> getFilters(SearchItemQuery dto);

    void insert(Long id);

    void delete(Long id);
}
