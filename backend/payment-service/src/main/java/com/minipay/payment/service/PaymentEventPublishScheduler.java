package com.minipay.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublishScheduler {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublishScheduler.class);

    private final PaymentEventPublishService paymentEventPublishService;

    public PaymentEventPublishScheduler(PaymentEventPublishService paymentEventPublishService) {
        this.paymentEventPublishService = paymentEventPublishService;
    }

    @Scheduled(
            fixedDelayString = "${minipay.payment.event-publish-fixed-delay-ms:30000}",
            initialDelayString = "${minipay.payment.event-publish-initial-delay-ms:15000}")
    public void retryPublishableEvents() {
        try {
            paymentEventPublishService.retryPublishableEvents();
        } catch (RuntimeException ex) {
            log.warn("Failed to scan payment events for RabbitMQ publishing", ex);
        }
    }
}
