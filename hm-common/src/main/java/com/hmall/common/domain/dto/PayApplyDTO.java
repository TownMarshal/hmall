package com.hmall.common.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "支付下单表单实体")
/*
 * @Builder
 * 它作用于类，将其变成建造者模式
 * 可以以链的形式调用
 * 初始化实例对象生成的对象是不可以变的，可以在创建对象的时候进行赋值
 * 如果需要在原来的基础上修改可以加 set 方法，final 字段可以不需要初始化
 * 它会生成一个全参的构造函数
 */
public class PayApplyDTO {
    @ApiModelProperty("业务订单id不能为空")
    @NotNull(message = "业务订单id不能为空")
    private Long bizOrderNo;
    @ApiModelProperty("支付金额必须为正数")
    @Min(value = 1, message = "支付金额必须为正数")
    private Integer amount;
    @ApiModelProperty("支付渠道编码不能为空")
    @NotNull(message = "支付渠道编码不能为空")
    private String payChannelCode;
    @ApiModelProperty("支付方式不能为空")
    @NotNull(message = "支付方式不能为空")
    private Integer payType;
    @ApiModelProperty("订单中的商品信息不能为空")
    @NotNull(message = "订单中的商品信息不能为空")
    private String orderInfo;
}