package com.minipay.order.listener;

import com.minipay.order.constant.OrderCacheConstant;
import com.minipay.order.enums.OrderOperateType;
import com.minipay.order.service.OrderService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpireListener extends KeyExpirationEventMessageListener {
    private final OrderService orderService;

    public RedisKeyExpireListener(RedisMessageListenerContainer container, OrderService orderService) {
        super(container);
        this.orderService = orderService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expireKey = new String(message.getBody());
        if (!expireKey.startsWith(OrderCacheConstant.ORDER_INFO_KEY)) {
            return;
        }
        String orderNo = expireKey.replace(OrderCacheConstant.ORDER_INFO_KEY, "");
        try {
            orderService.updateOrderStatus(orderNo, OrderOperateType.EXPIRE, null);
        } catch (Exception ignored) {
            // 订单已是终态，无需处理
        }
    }
}