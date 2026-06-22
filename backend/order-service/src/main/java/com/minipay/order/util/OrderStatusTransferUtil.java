package com.minipay.order.util;

import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.order.enums.OrderOperateType;
import com.minipay.order.enums.OrderStatus;
import java.util.HashMap;
import java.util.Map;

public class OrderStatusTransferUtil {
    // 流转规则：当前状态 -> {操作:目标状态}
    private static final Map<OrderStatus, Map<OrderOperateType, OrderStatus>> TRANSFER_MAP;

    static {
        TRANSFER_MAP = new HashMap<>();
        // PENDING 待支付
        Map<OrderOperateType, OrderStatus> pending = new HashMap<>();
        pending.put(OrderOperateType.PAY_START, OrderStatus.PAYING);
        pending.put(OrderOperateType.PAY_SUCCESS, OrderStatus.PAID);
        pending.put(OrderOperateType.EXPIRE, OrderStatus.EXPIRED);
        pending.put(OrderOperateType.CLOSE, OrderStatus.CLOSED);
        TRANSFER_MAP.put(OrderStatus.PENDING, pending);

        // PAYING 支付中
        Map<OrderOperateType, OrderStatus> paying = new HashMap<>();
        paying.put(OrderOperateType.PAY_SUCCESS, OrderStatus.PAID);
        paying.put(OrderOperateType.EXPIRE, OrderStatus.EXPIRED);
        paying.put(OrderOperateType.CLOSE, OrderStatus.CLOSED);
        TRANSFER_MAP.put(OrderStatus.PAYING, paying);

        // 终态：PAID / EXPIRED / CLOSED 不允许任何操作
        TRANSFER_MAP.put(OrderStatus.PAID, new HashMap<>());
        TRANSFER_MAP.put(OrderStatus.EXPIRED, new HashMap<>());
        TRANSFER_MAP.put(OrderStatus.CLOSED, new HashMap<>());
    }

    public static OrderStatus getTargetStatus(OrderStatus current, OrderOperateType operate) {
        Map<OrderOperateType, OrderStatus> allowOps = TRANSFER_MAP.get(current);
        if (!allowOps.containsKey(operate)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                    String.format("订单当前状态[%s]不支持操作[%s]", current, operate));
        }
        return allowOps.get(operate);
    }

    public static boolean isFinalStatus(OrderStatus status) {
        return OrderStatus.PAID == status || OrderStatus.EXPIRED == status || OrderStatus.CLOSED == status;
    }
}