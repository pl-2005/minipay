package com.minipay.payment.service;

import java.time.LocalDateTime;

public record PaymentConfirmResponse(
        String paymentNo,
        String orderNo,
        String status,
        LocalDateTime paidAt
) {
}
