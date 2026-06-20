package com.minipay.auth.service;

public record LoginResponse(
        String token,
        String username,
        String role,
        long expiresAt
) {
}
