package com.minipay.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minipay.common.messaging.PaymentEventMessaging;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final long confirmTimeoutMs;

    public PaymentEventPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${minipay.payment.rabbit-confirm-timeout-ms:5000}") long confirmTimeoutMs) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.confirmTimeoutMs = confirmTimeoutMs;
    }

    public void publishPaymentSuccess(String eventId, String orderNo) {
        CorrelationData correlationData = new CorrelationData(eventId);
        rabbitTemplate.convertAndSend(
                PaymentEventMessaging.EXCHANGE,
                PaymentEventMessaging.PAYMENT_SUCCESS_ROUTING_KEY,
                toMessage(eventId, orderNo),
                correlationData);
        waitForBrokerAck(eventId, correlationData);
    }

    private void waitForBrokerAck(String eventId, CorrelationData correlationData) {
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture().get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (!confirm.isAck()) {
                throw new IllegalStateException("RabbitMQ nacked payment event " + eventId + ": " + confirm.getReason());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for RabbitMQ confirm: " + eventId, ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("Timed out waiting for RabbitMQ confirm: " + eventId, ex);
        }
    }

    private String toMessage(String eventId, String orderNo) {
        try {
            return objectMapper.writeValueAsString(new PaymentSuccessMessage(eventId, orderNo));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to serialize payment event message", ex);
        }
    }

    private record PaymentSuccessMessage(String eventId, String orderNo) {
    }
}
