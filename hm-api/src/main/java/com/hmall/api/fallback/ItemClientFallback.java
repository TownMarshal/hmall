package com.hmall.api.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.domain.PageQuery;
import com.hmall.common.domain.dto.ItemDTO;
import com.hmall.common.domain.dto.OrderDetailDTO;
import com.hmall.common.domain.po.OrderDetail;
import com.hmall.common.domain.query.ItemPageQuery;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Administrator
 */
@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public PageDTO<ItemDTO> queryItemByPage(PageQuery query) {
                return null;
            }

            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！", cause);
                return CollUtils.emptyList();
            }

            @Override
            public PageDTO<ItemDTO> search(ItemPageQuery query) {
                log.error("商品搜索查询失败！", cause);
                return null;
            }

            @Override
            public ItemDTO queryItemById(Long id) {
                log.error("单件商品搜索查询失败！", cause);
                return null;
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.error("扣减商品库存失败！", cause);
//                throw new RuntimeException(cause);
                throw new BizIllegalException(cause);
            }

            @Override
            public void createStock(List<OrderDetail> details) {
                throw new BizIllegalException(cause);
            }


        };
    }
}