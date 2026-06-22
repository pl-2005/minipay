package com.minipay.auth.controller;

import com.minipay.auth.service.AuthService;
import com.minipay.auth.service.LoginRequest;
import com.minipay.auth.service.LoginResponse;
import com.minipay.auth.service.RegisterMerchantRequest;
import com.minipay.auth.service.RegisterMerchantResponse;
import com.minipay.common.api.ApiResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/api/auth/register/merchant")
    public ApiResponse<RegisterMerchantResponse> registerMerchant(
            @Valid @RequestBody RegisterMerchantRequest request) {
        return ApiResponse.success(authService.registerMerchant(request));
    }
}
