package com.minipay.order.mq.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaySuccessEvent(
        String orderNo,
        String thirdTradeNo,
        BigDecimal payAmount,
        LocalDateTime payTime
) implements Serializable {}