package com.hmall.common.domain.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索条件")
public class SearchItemQuery {
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer DEFAULT_PAGE_NUM = 1;
    @ApiModelProperty("搜索关键字")
    private String key;

    @ApiModelProperty("第几页")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = DEFAULT_PAGE_NUM;

    @ApiModelProperty("每页多少条")
    @Min(value = 1, message = "每页查询数量不能小于1")
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    @ApiModelProperty("排序方式")
    private String sortBy;
    @ApiModelProperty("是否升序")
    private Boolean isAsc = true;
    @ApiModelProperty("商品分类类目名称")
    private String category;
    @ApiModelProperty("品牌名称")
    private String brand;
    @ApiModelProperty("价格最小值")
    private Integer minPrice;
    @ApiModelProperty("价格最大值")
    private Integer maxPrice;

    public int from() {
        return (pageNo - 1) * pageSize;
    }

    public <T> Page<T> toMpPage(OrderItem... orderItems) {
        Page<T> page = new Page<>(pageNo, pageSize);
        // 是否手动指定排序方式
        if (orderItems != null && orderItems.length > 0) {
            for (OrderItem orderItem : orderItems) {
                page.addOrder(orderItem);
            }
            return page;
        }
        // 前端是否有排序字段
        if (StrUtil.isNotEmpty(sortBy)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(isAsc);
            orderItem.setColumn(sortBy);
            page.addOrder(orderItem);
        }
        return page;
    }

    public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc) {
        if (StringUtils.isBlank(sortBy)) {
            sortBy = defaultSortBy;
            this.isAsc = isAsc;
        }
        Page<T> page = new Page<>(pageNo, pageSize);
        OrderItem orderItem = new OrderItem();
        orderItem.setAsc(this.isAsc);
        orderItem.setColumn(sortBy);
        page.addOrder(orderItem);
        return page;
    }

    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return toMpPage("create_time", false);
    }
}