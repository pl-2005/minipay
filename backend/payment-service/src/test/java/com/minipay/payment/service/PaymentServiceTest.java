package com.minipay.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.minipay.common.api.BusinessException;
import com.minipay.payment.repository.PaymentRepository;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

class PaymentServiceTest {
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final PaymentEventPublishService publisher = mock(PaymentEventPublishService.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
    private final PaymentService paymentService = new PaymentService(
            paymentRepository,
            publisher,
            transactionTemplate);

    @Test
    void rejectsPaymentOutsideCurrentUsersMerchants() {
        when(paymentRepository.userCanAccessOrder("merchant-sunrise", "PSEED10001003"))
                .thenReturn(false);

        assertThatThrownBy(() -> paymentService.confirm(
                "merchant-sunrise",
                "PSEED10001003",
                new ConfirmPaymentRequest(new BigDecimal("1299.00"), "CROSS-MERCHANT")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("order not found");

        verifyNoInteractions(publisher, transactionTemplate);
    }
}
