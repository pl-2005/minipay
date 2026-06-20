package com.minipay.admin.service;

import java.time.LocalDateTime;

public record AdminNotificationResponse(
        String notifyNo,
        String merchantNo,
        String orderNo,
        String callbackUrl,
        String status,
        String responseBody,
        Integer retryCount,
        LocalDateTime nextRetryAt,
        LocalDateTime createdAt
) {
}
