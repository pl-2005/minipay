package com.minipay.order.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String PAY_SUCCESS_QUEUE = "pay.success.order.queue";

    @Bean
    public Queue paySuccessQueue() {
        return new Queue(PAY_SUCCESS_QUEUE, true);
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(ConnectionFactory factory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
        container.setQueueNames(PAY_SUCCESS_QUEUE);
        container.setConcurrentConsumers(3);
        return container;
    }
}