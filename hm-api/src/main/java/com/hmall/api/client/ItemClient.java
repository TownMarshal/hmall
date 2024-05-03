package com.hmall.api.client;


import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.fallback.ItemClientFallback;
import com.hmall.common.domain.dto.ItemDTO;
import com.hmall.common.domain.dto.OrderDetailDTO;
import com.hmall.common.domain.po.OrderDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);

    @PutMapping("/items/add/deduct")
    public void createStock(List<OrderDetail> details);
}
