package com.minipay.auth.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterMerchantRequest(
        @NotBlank
        @Size(min = 4, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$")
        String username,

        @NotBlank
        @Size(min = 8, max = 72)
        String password,

        @NotBlank
        @Size(min = 2, max = 128)
        String merchantName,

        @Size(max = 255)
        String callbackUrl
) {
}
