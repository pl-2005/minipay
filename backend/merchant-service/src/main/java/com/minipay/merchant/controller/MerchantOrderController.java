package com.minipay.merchant.controller;

import java.util.List;

import com.minipay.common.api.ApiResponse;
import com.minipay.merchant.service.CreateOrderRequest;
import com.minipay.merchant.service.MerchantOrderService;
import com.minipay.merchant.service.OrderResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerchantOrderController {
    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    @PostMapping("/api/merchant/orders")
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(merchantOrderService.createOrder(request));
    }

    @GetMapping("/api/merchant/orders")
    public ApiResponse<List<OrderResponse>> listOrders(
            @RequestParam(name = "merchantNo", defaultValue = "M10001") String merchantNo,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.success(merchantOrderService.listOrders(merchantNo, keyword, status));
    }
}
