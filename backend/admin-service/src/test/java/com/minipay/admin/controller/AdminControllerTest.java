package com.minipay.admin.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.minipay.admin.service.AdminQueryService;

import org.junit.jupiter.api.Test;

class AdminControllerTest {
    private final AdminQueryService queryService = mock(AdminQueryService.class);
    private final AdminController controller = new AdminController(queryService);

    @Test
    void forwardsOrderFilters() {
        when(queryService.listOrders("order", "PAID")).thenReturn(List.of());

        controller.listOrders("order", "PAID");

        verify(queryService).listOrders("order", "PAID");
    }

    @Test
    void forwardsPaymentFilters() {
        when(queryService.listPayments("payment", "SUCCESS")).thenReturn(List.of());

        controller.listPayments("payment", "SUCCESS");

        verify(queryService).listPayments("payment", "SUCCESS");
    }

    @Test
    void forwardsEventFilters() {
        when(queryService.listEvents("event", "PAYMENT_SUCCESS", "DONE")).thenReturn(List.of());

        controller.listEvents("event", "PAYMENT_SUCCESS", "DONE");

        verify(queryService).listEvents("event", "PAYMENT_SUCCESS", "DONE");
    }

    @Test
    void forwardsNotificationFilters() {
        when(queryService.listNotifications("notify", "RETRY")).thenReturn(List.of());

        controller.listNotifications("notify", "RETRY");

        verify(queryService).listNotifications("notify", "RETRY");
    }
}
