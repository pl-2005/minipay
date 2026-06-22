package com.minipay.merchant.controller;

import java.util.List;

import com.minipay.common.api.ApiResponse;
import com.minipay.merchant.service.CreateOrderRequest;
import com.minipay.merchant.service.MerchantOptionResponse;
import com.minipay.merchant.service.MerchantOrderService;
import com.minipay.merchant.service.OrderResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerchantOrderController {
    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    @PostMapping("/api/merchant/orders")
    public ApiResponse<OrderResponse> createOrder(
            @RequestHeader("X-MiniPay-Username") String username,
            @Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(merchantOrderService.createOrder(username, request));
    }

    @GetMapping("/api/merchant/merchants")
    public ApiResponse<List<MerchantOptionResponse>> listMerchants(
            @RequestHeader("X-MiniPay-Username") String username) {
        return ApiResponse.success(merchantOrderService.listActiveMerchants(username));
    }

    @GetMapping("/api/merchant/orders")
    public ApiResponse<List<OrderResponse>> listOrders(
            @RequestHeader("X-MiniPay-Username") String username,
            @RequestParam(name = "merchantNo") String merchantNo,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.success(merchantOrderService.listOrders(username, merchantNo, keyword, status));
    }
}
