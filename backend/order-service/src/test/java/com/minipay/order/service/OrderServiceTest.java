package com.minipay.order.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.minipay.common.api.BusinessException;
import com.minipay.order.repository.PayOrderRepository;

import org.junit.jupiter.api.Test;

class OrderServiceTest {
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final OrderService orderService = new OrderService(payOrderRepository);

    @Test
    void hidesOrdersOutsideCurrentUsersMerchants() {
        when(payOrderRepository.findByOrderNoForUser("merchant-sunrise", "PSEED10001001"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder("merchant-sunrise", "PSEED10001001"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("order not found");
    }
}
