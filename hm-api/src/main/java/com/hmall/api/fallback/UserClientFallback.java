package com.hmall.api.fallback;

import com.hmall.api.client.UserClient;
import com.hmall.common.exception.BizIllegalException;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * @author Administrator
 */
public class UserClientFallback implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public void deductMoney(String pw, Integer amount) {
                throw new BizIllegalException(cause);
            }
        };
    }
}
