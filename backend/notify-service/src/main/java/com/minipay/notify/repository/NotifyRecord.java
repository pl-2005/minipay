package com.minipay.notify.repository;

import java.time.LocalDateTime;

public record NotifyRecord(
        long id,
        String notifyNo,
        String merchantNo,
        String orderNo,
        String callbackUrl,
        String status,
        String responseBody,
        int retryCount,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
