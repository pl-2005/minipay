package com.minipay.merchant.service;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotBlank String merchantNo,
        @NotBlank String merchantOrderNo,
        @NotBlank String subject,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        String callbackUrl
) {
}
