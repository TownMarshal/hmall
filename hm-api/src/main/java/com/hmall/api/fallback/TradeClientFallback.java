package com.hmall.api.fallback;

import com.hmall.api.client.TradeClient;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author Administrator
 */
public class TradeClientFallback implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {
            @Override
            public void markOrderPaySuccess(Long orderId) {
                throw new BizIllegalException(cause);
            }
        };
    }
}
