package com.minipay.common.api;

public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "request validation failed"),
    UNAUTHORIZED("UNAUTHORIZED", "authentication required"),
    FORBIDDEN("FORBIDDEN", "permission denied"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "order not found"),
    ORDER_NOT_PAYABLE("ORDER_NOT_PAYABLE", "order is not payable"),
    AMOUNT_MISMATCH("AMOUNT_MISMATCH", "payment amount does not match order amount"),
    DUPLICATE_PAYMENT("DUPLICATE_PAYMENT", "duplicate payment request"),
    MESSAGE_PUBLISH_FAILED("MESSAGE_PUBLISH_FAILED", "payment event was not accepted by message broker"),
    MERCHANT_NOT_FOUND("MERCHANT_NOT_FOUND", "merchant not found"),
    INTERNAL_ERROR("INTERNAL_ERROR", "internal server error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
