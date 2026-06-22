package com.minipay.merchant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.minipay.common.api.BusinessException;
import com.minipay.merchant.repository.Merchant;
import com.minipay.merchant.repository.MerchantRepository;
import com.minipay.merchant.repository.PayOrderRepository;

import org.junit.jupiter.api.Test;

class MerchantOrderServiceTest {
    private final MerchantRepository merchantRepository = mock(MerchantRepository.class);
    private final PayOrderRepository payOrderRepository = mock(PayOrderRepository.class);
    private final MerchantOrderService service = new MerchantOrderService(merchantRepository, payOrderRepository);

    @Test
    void listsOnlyMerchantsLinkedToCurrentUser() {
        when(merchantRepository.listActiveForUser("merchant-demo"))
                .thenReturn(List.of(new Merchant(1L, "M10001", "Demo Merchant", "ACTIVE", null)));

        List<MerchantOptionResponse> merchants = service.listActiveMerchants("merchant-demo");

        assertThat(merchants).containsExactly(new MerchantOptionResponse("M10001", "Demo Merchant"));
        verify(merchantRepository).listActiveForUser("merchant-demo");
    }

    @Test
    void rejectsOrderAccessOutsideCurrentUsersMerchants() {
        when(merchantRepository.findActiveForUser("merchant-sunrise", "M10001"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listOrders(
                "merchant-sunrise",
                "M10001",
                null,
                null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("merchant not found");
    }
}
