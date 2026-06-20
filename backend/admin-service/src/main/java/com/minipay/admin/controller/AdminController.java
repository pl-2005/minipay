package com.minipay.admin.controller;

import java.util.List;

import com.minipay.admin.service.AdminEventResponse;
import com.minipay.admin.service.AdminNotificationResponse;
import com.minipay.admin.service.AdminOrderResponse;
import com.minipay.admin.service.AdminPaymentResponse;
import com.minipay.admin.service.AdminQueryService;
import com.minipay.common.api.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
    private final AdminQueryService adminQueryService;

    public AdminController(AdminQueryService adminQueryService) {
        this.adminQueryService = adminQueryService;
    }

    @GetMapping("/api/admin/orders")
    public ApiResponse<List<AdminOrderResponse>> listOrders(
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.success(adminQueryService.listOrders(status));
    }

    @GetMapping("/api/admin/payments")
    public ApiResponse<List<AdminPaymentResponse>> listPayments() {
        return ApiResponse.success(adminQueryService.listPayments());
    }

    @GetMapping("/api/admin/events")
    public ApiResponse<List<AdminEventResponse>> listEvents() {
        return ApiResponse.success(adminQueryService.listEvents());
    }

    @GetMapping("/api/admin/notifications")
    public ApiResponse<List<AdminNotificationResponse>> listNotifications() {
        return ApiResponse.success(adminQueryService.listNotifications());
    }
}
