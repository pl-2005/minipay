package com.minipay.admin.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminPaymentResponse(
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
