package com.minipay.notify.config;

import com.minipay.common.messaging.PaymentEventMessaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifyRabbitConfig {
    @Bean
    DirectExchange paymentEventExchange() {
        return ExchangeBuilder.directExchange(PaymentEventMessaging.EXCHANGE).durable(true).build();
    }

    @Bean
    Queue paymentSuccessQueue() {
        return QueueBuilder.durable(PaymentEventMessaging.PAYMENT_SUCCESS_QUEUE).build();
    }

    @Bean
    Binding paymentSuccessBinding(Queue paymentSuccessQueue, DirectExchange paymentEventExchange) {
        return BindingBuilder.bind(paymentSuccessQueue)
                .to(paymentEventExchange)
                .with(PaymentEventMessaging.PAYMENT_SUCCESS_ROUTING_KEY);
    }
}
