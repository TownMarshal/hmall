package com.hmall.api.fallback;

import com.hmall.api.client.PayClient;
import com.hmall.common.domain.dto.PayOrderDTO;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author Administrator
 */
public class PayClientFallback implements FallbackFactory<PayClient> {
    @Override
    public PayClient create(Throwable cause) {
        return new PayClient() {

            @Override
            public PayOrderDTO queryPayOrderByBizOrderNo(Long id) {
                return null;
            }

            @Override
            public void update(PayOrderDTO payOrderDTO) {
                throw new BizIllegalException(cause);
            }

        };
    }
}
