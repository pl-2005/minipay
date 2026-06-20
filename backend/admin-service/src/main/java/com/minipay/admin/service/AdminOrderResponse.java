package com.minipay.admin.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderResponse(
        String orderNo,
        String merchantNo,
        String merchantOrderNo,
        String subject,
        BigDecimal amount,
        String status,
        LocalDateTime expireAt,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}
