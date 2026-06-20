package com.minipay.auth.repository;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        String role,
        String status
) {
}
