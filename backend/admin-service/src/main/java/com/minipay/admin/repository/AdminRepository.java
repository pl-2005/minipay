package com.minipay.admin.repository;

import java.util.List;

import com.minipay.admin.service.AdminEventResponse;
import com.minipay.admin.service.AdminNotificationResponse;
import com.minipay.admin.service.AdminOrderResponse;
import com.minipay.admin.service.AdminPaymentResponse;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {
    private final JdbcClient jdbcClient;

    public AdminRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AdminOrderResponse> listOrders(String status) {
        if (status == null || status.isBlank()) {
            return jdbcClient.sql("""
                            SELECT order_no, merchant_no, merchant_order_no, subject, amount, status,
                                   expire_at, paid_at, created_at
                            FROM pay_order
                            ORDER BY created_at DESC
                            LIMIT 100
                            """)
                    .query((rs, rowNum) -> new AdminOrderResponse(
                            rs.getString("order_no"),
                            rs.getString("merchant_no"),
                            rs.getString("merchant_order_no"),
                            rs.getString("subject"),
                            rs.getBigDecimal("amount"),
                            rs.getString("status"),
                            rs.getTimestamp("expire_at") == null ? null : rs.getTimestamp("expire_at").toLocalDateTime(),
                            rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
                            rs.getTimestamp("created_at").toLocalDateTime()))
                    .list();
        }

        return jdbcClient.sql("""
                        SELECT order_no, merchant_no, merchant_order_no, subject, amount, status,
                               expire_at, paid_at, created_at
                        FROM pay_order
                        WHERE status = :status
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .param("status", status)
                .query((rs, rowNum) -> new AdminOrderResponse(
                        rs.getString("order_no"),
                        rs.getString("merchant_no"),
                        rs.getString("merchant_order_no"),
                        rs.getString("subject"),
                        rs.getBigDecimal("amount"),
                        rs.getString("status"),
                        rs.getTimestamp("expire_at") == null ? null : rs.getTimestamp("expire_at").toLocalDateTime(),
                        rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                .list();
    }

    public List<AdminPaymentResponse> listPayments() {
        return jdbcClient.sql("""
                        SELECT payment_no, order_no, merchant_no, amount, status, idempotency_key,
                               paid_at, created_at
                        FROM payment_record
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .query((rs, rowNum) -> new AdminPaymentResponse(
                        rs.getString("payment_no"),
                        rs.getString("order_no"),
                        rs.getString("merchant_no"),
                        rs.getBigDecimal("amount"),
                        rs.getString("status"),
                        rs.getString("idempotency_key"),
                        rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                .list();
    }

    public List<AdminEventResponse> listEvents() {
        return jdbcClient.sql("""
                        SELECT event_id, event_type, aggregate_no, payload, status, retry_count, created_at
                        FROM payment_event
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .query((rs, rowNum) -> new AdminEventResponse(
                        rs.getString("event_id"),
                        rs.getString("event_type"),
                        rs.getString("aggregate_no"),
                        rs.getString("payload"),
                        rs.getString("status"),
                        rs.getInt("retry_count"),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                .list();
    }

    public List<AdminNotificationResponse> listNotifications() {
        return jdbcClient.sql("""
                        SELECT notify_no, merchant_no, order_no, callback_url, status,
                               response_body, retry_count, next_retry_at, created_at
                        FROM notify_record
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .query((rs, rowNum) -> new AdminNotificationResponse(
                        rs.getString("notify_no"),
                        rs.getString("merchant_no"),
                        rs.getString("order_no"),
                        rs.getString("callback_url"),
                        rs.getString("status"),
                        rs.getString("response_body"),
                        rs.getInt("retry_count"),
                        rs.getTimestamp("next_retry_at") == null ? null : rs.getTimestamp("next_retry_at").toLocalDateTime(),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                .list();
    }
}
