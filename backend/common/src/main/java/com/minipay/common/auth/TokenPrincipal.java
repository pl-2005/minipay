package com.minipay.common.auth;

public record TokenPrincipal(
        String username,
        String role,
        long expiresAt
) {
}
