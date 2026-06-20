package com.minipay.notify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotifyRetryScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotifyRetryScheduler.class);

    private final PaymentNotificationService paymentNotificationService;

    public NotifyRetryScheduler(PaymentNotificationService paymentNotificationService) {
        this.paymentNotificationService = paymentNotificationService;
    }

    @Scheduled(
            fixedDelayString = "${minipay.notify.retry-fixed-delay-ms:30000}",
            initialDelayString = "${minipay.notify.retry-initial-delay-ms:10000}")
    public void retryDueNotifications() {
        try {
            paymentNotificationService.retryDueNotifications();
        } catch (RuntimeException ex) {
            log.warn("Failed to scan due notifications", ex);
        }
    }
}
