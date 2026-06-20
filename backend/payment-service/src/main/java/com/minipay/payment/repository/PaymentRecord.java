package com.minipay.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRecord(
        Long id,
        String paymentNo,
        String orderNo,
        String merchantNo,
        BigDecimal amount,
        String status,
        String idempotencyKey,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}
