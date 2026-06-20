package com.minipay.merchant.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        String orderNo,
        String merchantNo,
        String merchantOrderNo,
        String subject,
        BigDecimal amount,
        String status,
        String payUrl,
        LocalDateTime expireAt,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}
