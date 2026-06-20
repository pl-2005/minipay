package com.minipay.payment.controller;

import com.minipay.common.api.ApiResponse;
import com.minipay.payment.service.ConfirmPaymentRequest;
import com.minipay.payment.service.PaymentConfirmResponse;
import com.minipay.payment.service.PaymentService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/api/pay/orders/{orderNo}/confirm")
    public ApiResponse<PaymentConfirmResponse> confirm(
            @PathVariable("orderNo") String orderNo,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        return ApiResponse.success(paymentService.confirm(orderNo, request));
    }
}
