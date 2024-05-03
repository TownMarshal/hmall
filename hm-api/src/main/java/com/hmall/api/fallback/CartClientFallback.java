package com.hmall.api.fallback;

import com.hmall.api.client.CartClient;
import com.hmall.common.exception.BizIllegalException;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;

/**
 * @author Administrator
 */
public class CartClientFallback implements FallbackFactory<CartClient> {

    @Override
    public CartClient create(Throwable cause) {
        return new CartClient() {
            @Override
            public void deleteCartItemByIds(Collection<Long> ids) {
                throw new BizIllegalException(cause);
            }
        };
    }
}
