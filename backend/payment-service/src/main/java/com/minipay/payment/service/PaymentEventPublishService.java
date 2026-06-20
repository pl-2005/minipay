package com.minipay.payment.service;

import java.util.List;

import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.payment.repository.PaymentEventRecord;
import com.minipay.payment.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventPublishService {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublishService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Value("${minipay.payment.event-publish-batch-size:20}")
    private int eventPublishBatchSize;

    public PaymentEventPublishService(
            PaymentRepository paymentRepository,
            PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public void publishRequired(String eventId, String orderNo) {
        try {
            publish(eventId, orderNo);
        } catch (RuntimeException ex) {
            paymentRepository.markPaymentEventPublishFailed(eventId);
            throw new BusinessException(
                    ErrorCode.MESSAGE_PUBLISH_FAILED,
                    "payment succeeded, but RabbitMQ did not accept payment event " + eventId);
        }
    }

    public void retryPublishableEvents() {
        List<PaymentEventRecord> events = paymentRepository.findPublishablePaymentEvents(eventPublishBatchSize);
        for (PaymentEventRecord event : events) {
            try {
                publish(event.eventId(), event.aggregateNo());
                log.info("Republished payment event {} to RabbitMQ", event.eventId());
            } catch (RuntimeException ex) {
                paymentRepository.markPaymentEventPublishFailed(event.eventId());
                log.warn("Failed to republish payment event {}", event.eventId(), ex);
            }
        }
    }

    private void publish(String eventId, String orderNo) {
        paymentEventPublisher.publishPaymentSuccess(eventId, orderNo);
        paymentRepository.markPaymentEventPublished(eventId);
    }
}
