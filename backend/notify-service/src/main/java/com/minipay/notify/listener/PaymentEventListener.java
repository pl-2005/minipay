package com.minipay.notify.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minipay.common.messaging.PaymentEventMessaging;
import com.minipay.notify.service.PaymentNotificationService;
import com.minipay.notify.service.PaymentSuccessMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final ObjectMapper objectMapper;
    private final PaymentNotificationService paymentNotificationService;

    public PaymentEventListener(ObjectMapper objectMapper, PaymentNotificationService paymentNotificationService) {
        this.objectMapper = objectMapper;
        this.paymentNotificationService = paymentNotificationService;
    }

    @RabbitListener(queues = PaymentEventMessaging.PAYMENT_SUCCESS_QUEUE)
    public void onPaymentSuccess(String messageBody) {
        try {
            PaymentSuccessMessage message = objectMapper.readValue(messageBody, PaymentSuccessMessage.class);
            paymentNotificationService.handlePaymentSuccess(message);
        } catch (JsonProcessingException ex) {
            log.warn("Dropped malformed payment success message: {}", messageBody, ex);
        } catch (RuntimeException ex) {
            log.warn("Failed to process payment success message, RabbitMQ will redeliver if configured: {}", messageBody, ex);
            throw ex;
        }
    }
}
