package com.hmall.trade.listener;


import com.hmall.common.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;


    //使用lazy进行优化
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "trade.pay.success.queue", durable = "true", arguments = @Argument(name = "x-queue-mode", value = "lazy")), exchange = @Exchange(value = "pay.direct", type = ExchangeTypes.DIRECT), key = {"pay.success"}))
   /* @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.topic"),
            key = "pay.success"
    ))*/ public void listenPaySuccess(Long orderId) {
        Order order = orderService.getById(orderId);
        if (order == null || order.getStatus() != 1) {
            return;
        }
        orderService.markOrderPaySuccess(orderId);
    }
}