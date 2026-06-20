package com.minipay.notify.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.minipay.notify.repository.NotifyRecord;
import com.minipay.notify.repository.NotifyRepository;
import com.minipay.notify.repository.PaymentEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PaymentNotificationService {
    private static final Logger log = LoggerFactory.getLogger(PaymentNotificationService.class);
    private static final int RESPONSE_BODY_MAX_LENGTH = 2000;

    private final NotifyRepository notifyRepository;
    private final MerchantCallbackClient merchantCallbackClient;

    @Value("${minipay.notify.max-attempts:3}")
    private int maxAttempts;

    @Value("${minipay.notify.retry-batch-size:20}")
    private int retryBatchSize;

    public PaymentNotificationService(
            NotifyRepository notifyRepository,
            MerchantCallbackClient merchantCallbackClient) {
        this.notifyRepository = notifyRepository;
        this.merchantCallbackClient = merchantCallbackClient;
    }

    public void handlePaymentSuccess(PaymentSuccessMessage message) {
        if (message == null || !StringUtils.hasText(message.orderNo())) {
            log.warn("Ignored malformed payment success message: {}", message);
            return;
        }

        Optional<PaymentEvent> event = StringUtils.hasText(message.eventId())
                ? notifyRepository.findEventByEventId(message.eventId())
                : notifyRepository.findLatestEventByOrderNo(message.orderNo());

        event.or(() -> notifyRepository.findLatestEventByOrderNo(message.orderNo()))
                .ifPresentOrElse(
                        paymentEvent -> handleEvent(paymentEvent, "message"),
                        () -> log.warn("No payment event found for message {}", message));
    }

    public void retryDueNotifications() {
        for (NotifyRecord notifyRecord : notifyRepository.findDueNotifications(retryBatchSize)) {
            notifyRepository.findLatestEventByOrderNo(notifyRecord.orderNo())
                    .ifPresentOrElse(
                            paymentEvent -> processNotification(paymentEvent, notifyRecord, "retry-scan"),
                            () -> log.warn("No payment event found for due notification {}", notifyRecord.notifyNo()));
        }
    }

    private void handleEvent(PaymentEvent paymentEvent, String source) {
        notifyRepository.findLatestNotifyByOrderNo(paymentEvent.aggregateNo())
                .ifPresentOrElse(
                        notifyRecord -> processNotification(paymentEvent, notifyRecord, source),
                        () -> {
                            log.warn("No notification record found for payment event {}", paymentEvent.eventId());
                            notifyRepository.markEventFailed(paymentEvent.eventId());
                        });
    }

    private void processNotification(PaymentEvent paymentEvent, NotifyRecord notifyRecord, String source) {
        if ("SUCCESS".equals(notifyRecord.status())) {
            notifyRepository.markEventDone(paymentEvent.eventId());
            return;
        }

        if (!notifyRepository.claimNotification(notifyRecord.notifyNo())) {
            return;
        }

        CallbackResult callbackResult = merchantCallbackClient.send(notifyRecord, paymentEvent);
        String responseBody = trim(callbackResult.responseBody());
        if (callbackResult.success()) {
            notifyRepository.markNotifySuccess(notifyRecord.notifyNo(), responseBody);
            notifyRepository.markEventDone(paymentEvent.eventId());
            log.info("Notification {} completed from {}", notifyRecord.notifyNo(), source);
            return;
        }

        int nextRetryCount = notifyRecord.retryCount() + 1;
        if (nextRetryCount >= maxAttempts) {
            notifyRepository.markNotifyFailed(notifyRecord.notifyNo(), responseBody);
            notifyRepository.markEventFailed(paymentEvent.eventId());
            log.warn("Notification {} failed after {} attempts", notifyRecord.notifyNo(), nextRetryCount);
            return;
        }

        notifyRepository.markNotifyRetry(notifyRecord.notifyNo(), responseBody, nextRetryAt(nextRetryCount));
        notifyRepository.markEventRetry(paymentEvent.eventId());
        log.warn("Notification {} will retry, attempt {}", notifyRecord.notifyNo(), nextRetryCount);
    }

    private LocalDateTime nextRetryAt(int retryCount) {
        long delayMinutes = Math.min(30L, 1L << Math.max(0, retryCount - 1));
        return LocalDateTime.now().plusMinutes(delayMinutes);
    }

    private String trim(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= RESPONSE_BODY_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, RESPONSE_BODY_MAX_LENGTH);
    }
}
