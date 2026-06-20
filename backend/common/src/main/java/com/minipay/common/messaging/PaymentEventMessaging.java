package com.minipay.common.messaging;

public final class PaymentEventMessaging {
    public static final String EXCHANGE = "minipay.payment.events";
    public static final String PAYMENT_SUCCESS_QUEUE = "minipay.notify.payment-success";
    public static final String PAYMENT_SUCCESS_ROUTING_KEY = "payment.success";

    private PaymentEventMessaging() {
    }
}
