package com.minipay.auth.service;

public record RegisterMerchantResponse(
        String token,
        String username,
        String role,
        long expiresAt,
        String merchantNo,
        String merchantName
) {
}
