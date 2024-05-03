package com.hmall.common.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@ApiModel(description = "订单明细条目")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderDetailDTO {
    @ApiModelProperty("商品id")
    private Long itemId;
    @ApiModelProperty("商品购买数量")
    private Integer num;
}
