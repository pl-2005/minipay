package com.minipay.notify.repository;

import java.time.LocalDateTime;

public record PaymentEvent(
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
