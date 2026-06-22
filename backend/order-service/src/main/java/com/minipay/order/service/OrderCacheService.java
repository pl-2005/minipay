package com.minipay.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minipay.order.constant.OrderCacheConstant;
import com.minipay.order.repository.PayOrder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class OrderCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public OrderCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String getKey(String orderNo) {
        return OrderCacheConstant.ORDER_INFO_KEY + orderNo;
    }

    public void setOrderCache(PayOrder order) {
        String key = getKey(order.orderNo());
        redisTemplate.opsForValue().set(key, order, OrderCacheConstant.ORDER_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public PayOrder getOrderCache(String orderNo) {
        String key = getKey(orderNo);
        Object cacheVal = redisTemplate.opsForValue().get(key);
        if (cacheVal == null) {
            return null;
        }
        // 自动把LinkedHashMap转为PayOrder Record，兼容不带@class的旧数据
        return objectMapper.convertValue(cacheVal, PayOrder.class);
    }

    public void deleteCache(String orderNo) {
        redisTemplate.delete(getKey(orderNo));
    }
}