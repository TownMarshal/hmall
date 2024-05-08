package com.hmall.api.client;


import cn.hutool.core.util.StrUtil;
import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.fallback.ItemClientFallback;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.dto.ItemDTO;
import com.hmall.common.domain.dto.OrderDetailDTO;
import com.hmall.common.domain.po.Item;
import com.hmall.common.domain.po.OrderDetail;
import com.hmall.common.domain.query.ItemPageQuery;
import com.hmall.common.utils.BeanUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;


/**
 * @author Administrator
 */
@FeignClient(value = "item-service",
        configuration = DefaultFeignConfig.class,
        fallbackFactory = ItemClientFallback.class)
public interface ItemClient {
    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @GetMapping("/search/list")
    PageDTO<ItemDTO> search(ItemPageQuery query);

    @GetMapping("/items/{id}")
    ItemDTO queryItemById(@PathVariable("id") Long id);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    @PutMapping("/items/add/deduct")
    public void createStock(List<OrderDetail> details);
}
