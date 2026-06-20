package com.minipay.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailResponse(
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
