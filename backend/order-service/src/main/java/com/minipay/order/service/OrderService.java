package com.minipay.order.service;

import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.order.repository.PayOrder;
import com.minipay.order.repository.PayOrderRepository;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final PayOrderRepository payOrderRepository;

    public OrderService(PayOrderRepository payOrderRepository) {
        this.payOrderRepository = payOrderRepository;
    }

    public OrderDetailResponse getOrder(String username, String orderNo) {
        PayOrder order = payOrderRepository.findByOrderNoForUser(username, orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        return toResponse(order);
    }

    private OrderDetailResponse toResponse(PayOrder order) {
        return new OrderDetailResponse(
                order.orderNo(),
                order.merchantNo(),
                order.merchantOrderNo(),
                order.subject(),
                order.amount(),
                order.status(),
                order.expireAt(),
                order.paidAt(),
                order.createdAt());
    }
}
