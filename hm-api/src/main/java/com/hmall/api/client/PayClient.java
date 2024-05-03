package com.hmall.api.client;


import com.hmall.api.config.DefaultFeignConfig;
import com.hmall.api.fallback.PayClientFallback;
import com.hmall.common.domain.dto.PayOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * - PayClient：支付系统的Feign客户端
 * @author Administrator
 */

@FeignClient(value = "pay-service",
        configuration = DefaultFeignConfig.class,
        fallbackFactory = PayClientFallback.class)
public interface PayClient {
    /**
     * 根据交易订单id查询支付单
     */
    @GetMapping("/pay-orders/biz/{id}")
    PayOrderDTO queryPayOrderByBizOrderNo(@PathVariable("id") Long id);

    @PutMapping("/pay-orders/update")
    void update(@RequestBody PayOrderDTO payOrderDTO);
}