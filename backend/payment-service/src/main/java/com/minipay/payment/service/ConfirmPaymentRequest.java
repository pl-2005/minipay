package com.minipay.payment.service;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;

public record ConfirmPaymentRequest(
        @DecimalMin("0.01") BigDecimal amount,
        String idempotencyKey
) {
}
