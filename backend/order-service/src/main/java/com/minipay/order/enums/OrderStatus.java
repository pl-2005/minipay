package com.minipay.order.enums;

public enum OrderStatus {
    PENDING("待支付"),
    PAYING("支付中"),
    PAID("已支付"),
    EXPIRED("已过期"),
    CLOSED("已关闭");

    private final String desc;
    OrderStatus(String desc) {
        this.desc = desc;
    }
    public String getDesc() {
        return desc;
    }
}