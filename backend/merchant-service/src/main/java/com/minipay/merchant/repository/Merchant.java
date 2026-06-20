package com.minipay.merchant.repository;

public record Merchant(
        Long id,
        String merchantNo,
        String merchantName,
        String status,
        String callbackUrl
) {
}
