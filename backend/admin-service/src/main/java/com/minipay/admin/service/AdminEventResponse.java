package com.minipay.admin.service;

import java.time.LocalDateTime;

public record AdminEventResponse(
        String eventId,
        String eventType,
        String aggregateNo,
        String payload,
        String status,
        Integer retryCount,
        LocalDateTime createdAt
) {
}
