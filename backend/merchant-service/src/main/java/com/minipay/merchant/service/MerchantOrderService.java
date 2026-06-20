package com.minipay.merchant.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.merchant.repository.Merchant;
import com.minipay.merchant.repository.MerchantRepository;
import com.minipay.merchant.repository.PayOrder;
import com.minipay.merchant.repository.PayOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MerchantOrderService {
    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MerchantRepository merchantRepository;
    private final PayOrderRepository payOrderRepository;

    public MerchantOrderService(MerchantRepository merchantRepository, PayOrderRepository payOrderRepository) {
        this.merchantRepository = merchantRepository;
        this.payOrderRepository = payOrderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Merchant merchant = merchantRepository.findActiveByMerchantNo(request.merchantNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.MERCHANT_NOT_FOUND));

        return payOrderRepository.findByMerchantOrderNo(request.merchantNo(), request.merchantOrderNo())
                .map(this::toResponse)
                .orElseGet(() -> createNewOrder(request, merchant));
    }

    public List<OrderResponse> listOrders(String merchantNo, String keyword, String status) {
        merchantRepository.findActiveByMerchantNo(merchantNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.MERCHANT_NOT_FOUND));
        return payOrderRepository.listByMerchant(merchantNo, keyword, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse createNewOrder(CreateOrderRequest request, Merchant merchant) {
        LocalDateTime now = LocalDateTime.now();
        String orderNo = generateOrderNo(now);
        String callbackUrl = StringUtils.hasText(request.callbackUrl())
                ? request.callbackUrl()
                : merchant.callbackUrl();

        payOrderRepository.insert(
                orderNo,
                request.merchantNo(),
                request.merchantOrderNo(),
                request.subject(),
                request.amount(),
                callbackUrl,
                now.plusMinutes(30));

        PayOrder order = payOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "created order cannot be loaded"));
        return toResponse(order);
    }

    private String generateOrderNo(LocalDateTime now) {
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return "P" + ORDER_NO_TIME.format(now) + suffix;
    }

    private OrderResponse toResponse(PayOrder order) {
        return new OrderResponse(
                order.orderNo(),
                order.merchantNo(),
                order.merchantOrderNo(),
                order.subject(),
                order.amount(),
                order.status(),
                "/pay/" + order.orderNo(),
                order.expireAt(),
                order.paidAt(),
                order.createdAt());
    }
}
