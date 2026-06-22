package com.minipay.order.service;

import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.order.enums.OrderOperateType;
import com.minipay.order.enums.OrderStatus;
import com.minipay.order.repository.PayOrder;
import com.minipay.order.repository.PayOrderRepository;
import com.minipay.order.util.OrderStatusTransferUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class OrderService {
    private final PayOrderRepository payOrderRepository;
    private final OrderCacheService orderCacheService;

    public OrderService(PayOrderRepository payOrderRepository, OrderCacheService orderCacheService) {
        this.payOrderRepository = payOrderRepository;
        this.orderCacheService = orderCacheService;
    }

    // 订单详情查询（先缓存，再DB，回写缓存）
    public OrderDetailResponse getOrder(String orderNo) {
        PayOrder cache = orderCacheService.getOrderCache(orderNo);
        if (cache != null) {
            return convertToResp(cache);
        }
        PayOrder dbOrder = payOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        orderCacheService.setOrderCache(dbOrder);
        return convertToResp(dbOrder);
    }

    // 统一状态更新入口（支付成功/过期/关闭）
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderNo, OrderOperateType operate, LocalDateTime paidTime) {
        PayOrder order = payOrderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        OrderStatus currentStatus = OrderStatus.valueOf(order.status());

        OrderStatus target = OrderStatusTransferUtil.getTargetStatus(currentStatus, operate);

        payOrderRepository.updateStatus(orderNo, target.name(), paidTime);
        orderCacheService.deleteCache(orderNo);
    }

    private OrderDetailResponse convertToResp(PayOrder order) {
        return new OrderDetailResponse(
                order.orderNo(),
                order.merchantNo(),
                order.merchantOrderNo(),
                order.subject(),
                order.amount(),
                order.status(),
                order.expireAt(),
                order.paidAt(),
                order.createdAt()
        );
    }
}