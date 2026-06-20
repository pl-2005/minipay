package com.minipay.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayOrder(
        Long id,
        String orderNo,
        String merchantNo,
        String merchantOrderNo,
        String subject,
        BigDecimal amount,
        String status,
        String callbackUrl,
        LocalDateTime expireAt,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
