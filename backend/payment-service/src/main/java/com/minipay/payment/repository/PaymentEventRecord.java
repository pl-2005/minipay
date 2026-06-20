package com.minipay.payment.repository;

import java.time.LocalDateTime;

public record PaymentEventRecord(
        long id,
        String eventId,
        String eventType,
        String aggregateNo,
        String payload,
        String status,
        int retryCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
