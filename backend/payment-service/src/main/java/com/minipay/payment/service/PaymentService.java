package com.minipay.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.payment.repository.PayOrder;
import com.minipay.payment.repository.PaymentRecord;
import com.minipay.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
public class PaymentService {
    private static final DateTimeFormatter NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String DEFAULT_IDEMPOTENCY_KEY = "MANUAL_CONFIRM";
    private static final String DEFAULT_CALLBACK_URL = "http://merchant.example.local/pay/callback";

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublishService paymentEventPublishService;
    private final TransactionTemplate transactionTemplate;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentEventPublishService paymentEventPublishService,
            TransactionTemplate transactionTemplate) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublishService = paymentEventPublishService;
        this.transactionTemplate = transactionTemplate;
    }

    public PaymentConfirmResponse confirm(String username, String orderNo, ConfirmPaymentRequest request) {
        if (!paymentRepository.userCanAccessOrder(username, orderNo)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        String idempotencyKey = StringUtils.hasText(request.idempotencyKey())
                ? request.idempotencyKey()
                : DEFAULT_IDEMPOTENCY_KEY;

        PaymentConfirmWork work = transactionTemplate.execute(status ->
                paymentRepository.findPaymentByIdempotencyKey(orderNo, idempotencyKey)
                        .map(this::existingPaymentWork)
                        .orElseGet(() -> confirmNewPayment(orderNo, request, idempotencyKey)));

        if (work == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "payment transaction returned no result");
        }
        if (work.publishRequired()) {
            paymentEventPublishService.publishRequired(work.eventId(), work.orderNo());
        }

        return work.response();
    }

    private PaymentConfirmWork confirmNewPayment(
            String orderNo,
            ConfirmPaymentRequest request,
            String idempotencyKey) {
        PayOrder order = paymentRepository.findOrderForUpdate(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if ("PAID".equals(order.status())) {
            return paymentRepository.findLatestPaymentByOrderNo(orderNo)
                    .map(this::existingPaymentWork)
                    .orElse(new PaymentConfirmWork(
                            new PaymentConfirmResponse(null, order.orderNo(), "PAID", order.paidAt()),
                            null,
                            null));
        }

        if (!"PENDING".equals(order.status())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE);
        }

        if (order.expireAt() != null && order.expireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAYABLE, "order has expired");
        }

        if (request.amount() != null && request.amount().compareTo(order.amount()) != 0) {
            throw new BusinessException(ErrorCode.AMOUNT_MISMATCH);
        }

        LocalDateTime paidAt = LocalDateTime.now();
        String paymentNo = generateNo("PAY", paidAt);
        paymentRepository.markOrderPaid(order.orderNo(), paidAt);
        paymentRepository.insertPayment(
                paymentNo,
                order.orderNo(),
                order.merchantNo(),
                order.amount(),
                idempotencyKey,
                paidAt);

        String eventId = generateNo("EVT", paidAt);
        String notifyNo = generateNo("NTF", paidAt);
        paymentRepository.insertPaymentEvent(
                eventId,
                order.orderNo(),
                paymentSuccessPayload(order, paymentNo, paidAt));
        paymentRepository.insertNotifyRecord(
                notifyNo,
                order.merchantNo(),
                order.orderNo(),
                StringUtils.hasText(order.callbackUrl()) ? order.callbackUrl() : DEFAULT_CALLBACK_URL);

        return new PaymentConfirmWork(
                new PaymentConfirmResponse(paymentNo, order.orderNo(), "SUCCESS", paidAt),
                eventId,
                order.orderNo());
    }

    private PaymentConfirmResponse toResponse(PaymentRecord paymentRecord) {
        return new PaymentConfirmResponse(
                paymentRecord.paymentNo(),
                paymentRecord.orderNo(),
                paymentRecord.status(),
                paymentRecord.paidAt());
    }

    private PaymentConfirmWork existingPaymentWork(PaymentRecord paymentRecord) {
        return paymentRepository.findLatestPaymentEventByOrderNo(paymentRecord.orderNo())
                .filter(event -> "PENDING".equals(event.status()) || "PUBLISH_FAILED".equals(event.status()))
                .map(event -> new PaymentConfirmWork(toResponse(paymentRecord), event.eventId(), event.aggregateNo()))
                .orElse(new PaymentConfirmWork(toResponse(paymentRecord), null, null));
    }

    private String generateNo(String prefix, LocalDateTime now) {
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
        return prefix + NO_TIME.format(now) + suffix;
    }

    private String paymentSuccessPayload(PayOrder order, String paymentNo, LocalDateTime paidAt) {
        return "{"
                + "\"paymentNo\":\"" + json(paymentNo) + "\","
                + "\"orderNo\":\"" + json(order.orderNo()) + "\","
                + "\"merchantNo\":\"" + json(order.merchantNo()) + "\","
                + "\"amount\":" + order.amount().toPlainString() + ","
                + "\"status\":\"PAID\","
                + "\"paidAt\":\"" + json(paidAt.toString()) + "\""
                + "}";
    }

    private String json(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record PaymentConfirmWork(PaymentConfirmResponse response, String eventId, String orderNo) {
        boolean publishRequired() {
            return StringUtils.hasText(eventId) && StringUtils.hasText(orderNo);
        }
    }
}
