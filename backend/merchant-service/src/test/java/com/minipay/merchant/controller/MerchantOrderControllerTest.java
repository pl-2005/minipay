package com.minipay.merchant.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.minipay.merchant.service.MerchantOrderService;

import org.junit.jupiter.api.Test;

class MerchantOrderControllerTest {
    private final MerchantOrderService orderService = mock(MerchantOrderService.class);
    private final MerchantOrderController controller = new MerchantOrderController(orderService);

    @Test
    void forwardsMerchantOrderFilters() {
        when(orderService.listOrders("merchant-demo", "M10001", "order", "PAID")).thenReturn(List.of());

        controller.listOrders("merchant-demo", "M10001", "order", "PAID");

        verify(orderService).listOrders("merchant-demo", "M10001", "order", "PAID");
    }

    @Test
    void returnsActiveMerchantOptions() {
        when(orderService.listActiveMerchants("merchant-demo")).thenReturn(List.of());

        controller.listMerchants("merchant-demo");

        verify(orderService).listActiveMerchants("merchant-demo");
    }
}
