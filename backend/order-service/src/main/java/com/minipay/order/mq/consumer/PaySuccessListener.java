package com.minipay.order.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minipay.order.config.RabbitMqConfig;
import com.minipay.order.enums.OrderOperateType;
import com.minipay.order.mq.event.PaySuccessEvent;
import com.minipay.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaySuccessListener {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public PaySuccessListener(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMqConfig.PAY_SUCCESS_QUEUE)
    public void consumePaySuccessMsg(Message message) throws Exception {
        String json = new String(message.getBody());
        PaySuccessEvent event = objectMapper.readValue(json, PaySuccessEvent.class);
        // 执行支付成功状态变更
        orderService.updateOrderStatus(event.orderNo(), OrderOperateType.PAY_SUCCESS, event.payTime());
    }
}