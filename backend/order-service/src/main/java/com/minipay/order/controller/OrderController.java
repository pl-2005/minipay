package com.minipay.order.controller;

import com.minipay.common.api.ApiResponse;
import com.minipay.order.service.OrderDetailResponse;
import com.minipay.order.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/pay/orders/{orderNo}")
    public ApiResponse<OrderDetailResponse> getPayOrder(@PathVariable("orderNo") String orderNo) {
        return ApiResponse.success(orderService.getOrder(orderNo));
    }

    @GetMapping("/api/orders/{orderNo}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable("orderNo") String orderNo) {
        return ApiResponse.success(orderService.getOrder(orderNo));
    }
}
