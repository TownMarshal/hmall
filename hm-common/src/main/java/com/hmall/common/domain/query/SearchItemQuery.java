package com.hmall.common.domain.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchItemQuery {
    public static final Integer DEFAULT_PAGE_SIZE = 20;
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
}
