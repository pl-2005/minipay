package com.minipay.notify.service;

public record CallbackResult(boolean success, String responseBody) {
    public static CallbackResult success(String responseBody) {
        return new CallbackResult(true, responseBody);
    }

    public static CallbackResult failure(String responseBody) {
        return new CallbackResult(false, responseBody);
    }
}
