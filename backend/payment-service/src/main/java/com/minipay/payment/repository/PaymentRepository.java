package com.minipay.payment.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {
    private final JdbcClient jdbcClient;

    private static final RowMapper<PayOrder> ORDER_ROW_MAPPER = (rs, rowNum) -> new PayOrder(
            rs.getLong("id"),
            rs.getString("order_no"),
            rs.getString("merchant_no"),
            rs.getString("merchant_order_no"),
            rs.getString("subject"),
            rs.getBigDecimal("amount"),
            rs.getString("status"),
            rs.getString("callback_url"),
            rs.getTimestamp("expire_at") == null ? null : rs.getTimestamp("expire_at").toLocalDateTime(),
            rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime());

    private static final RowMapper<PaymentRecord> PAYMENT_ROW_MAPPER = (rs, rowNum) -> new PaymentRecord(
            rs.getLong("id"),
            rs.getString("payment_no"),
            rs.getString("order_no"),
            rs.getString("merchant_no"),
            rs.getBigDecimal("amount"),
            rs.getString("status"),
            rs.getString("idempotency_key"),
            rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime());

    private static final RowMapper<PaymentEventRecord> EVENT_ROW_MAPPER = (rs, rowNum) -> new PaymentEventRecord(
            rs.getLong("id"),
            rs.getString("event_id"),
            rs.getString("event_type"),
            rs.getString("aggregate_no"),
            rs.getString("payload"),
            rs.getString("status"),
            rs.getInt("retry_count"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime());

    public PaymentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean userCanAccessOrder(String username, String orderNo) {
        return jdbcClient.sql("""
                        SELECT EXISTS(
                            SELECT 1
                            FROM pay_order
                            JOIN merchant ON merchant.merchant_no = pay_order.merchant_no
                            JOIN merchant_user ON merchant_user.merchant_id = merchant.id
                            JOIN app_user ON app_user.id = merchant_user.user_id
                            WHERE app_user.username = :username
                              AND app_user.status = 'ACTIVE'
                              AND merchant.status = 'ACTIVE'
                              AND pay_order.order_no = :orderNo
                        )
                        """)
                .param("username", username)
                .param("orderNo", orderNo)
                .query(Boolean.class)
                .single();
    }

    public Optional<PayOrder> findOrderForUpdate(String orderNo) {
        return jdbcClient.sql("""
                        SELECT id, order_no, merchant_no, merchant_order_no, subject, amount, status,
                               callback_url, expire_at, paid_at, created_at, updated_at
                        FROM pay_order
                        WHERE order_no = :orderNo
                        FOR UPDATE
                        """)
                .param("orderNo", orderNo)
                .query(ORDER_ROW_MAPPER)
                .optional();
    }

    public Optional<PaymentRecord> findPaymentByIdempotencyKey(String orderNo, String idempotencyKey) {
        return jdbcClient.sql("""
                        SELECT id, payment_no, order_no, merchant_no, amount, status,
                               idempotency_key, paid_at, created_at
                        FROM payment_record
                        WHERE order_no = :orderNo AND idempotency_key = :idempotencyKey
                        """)
                .param("orderNo", orderNo)
                .param("idempotencyKey", idempotencyKey)
                .query(PAYMENT_ROW_MAPPER)
                .optional();
    }

    public Optional<PaymentRecord> findLatestPaymentByOrderNo(String orderNo) {
        return jdbcClient.sql("""
                        SELECT id, payment_no, order_no, merchant_no, amount, status,
                               idempotency_key, paid_at, created_at
                        FROM payment_record
                        WHERE order_no = :orderNo
                        ORDER BY created_at DESC
                        LIMIT 1
                        """)
                .param("orderNo", orderNo)
                .query(PAYMENT_ROW_MAPPER)
                .optional();
    }

    public Optional<PaymentEventRecord> findLatestPaymentEventByOrderNo(String orderNo) {
        return jdbcClient.sql("""
                        SELECT id, event_id, event_type, aggregate_no, payload, status,
                               retry_count, created_at, updated_at
                        FROM payment_event
                        WHERE aggregate_no = :orderNo AND event_type = 'PAYMENT_SUCCESS'
                        ORDER BY created_at DESC
                        LIMIT 1
                        """)
                .param("orderNo", orderNo)
                .query(EVENT_ROW_MAPPER)
                .optional();
    }

    public List<PaymentEventRecord> findPublishablePaymentEvents(int limit) {
        return jdbcClient.sql("""
                        SELECT id, event_id, event_type, aggregate_no, payload, status,
                               retry_count, created_at, updated_at
                        FROM payment_event
                        WHERE event_type = 'PAYMENT_SUCCESS'
                          AND status IN ('PENDING', 'PUBLISH_FAILED')
                          AND updated_at <= CURRENT_TIMESTAMP - INTERVAL '5 seconds'
                        ORDER BY updated_at ASC
                        LIMIT :limit
                        """)
                .param("limit", limit)
                .query(EVENT_ROW_MAPPER)
                .list();
    }

    public void markOrderPaid(String orderNo, LocalDateTime paidAt) {
        jdbcClient.sql("""
                        UPDATE pay_order
                        SET status = 'PAID',
                            paid_at = :paidAt,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE order_no = :orderNo AND status = 'PENDING'
                        """)
                .param("orderNo", orderNo)
                .param("paidAt", paidAt)
                .update();
    }

    public void insertPayment(
            String paymentNo,
            String orderNo,
            String merchantNo,
            BigDecimal amount,
            String idempotencyKey,
            LocalDateTime paidAt) {
        jdbcClient.sql("""
                        INSERT INTO payment_record (
                            payment_no, order_no, merchant_no, amount, status,
                            idempotency_key, paid_at
                        )
                        VALUES (
                            :paymentNo, :orderNo, :merchantNo, :amount, 'SUCCESS',
                            :idempotencyKey, :paidAt
                        )
                        """)
                .param("paymentNo", paymentNo)
                .param("orderNo", orderNo)
                .param("merchantNo", merchantNo)
                .param("amount", amount)
                .param("idempotencyKey", idempotencyKey)
                .param("paidAt", paidAt)
                .update();
    }

    public void insertPaymentEvent(String eventId, String orderNo, String payload) {
        jdbcClient.sql("""
                        INSERT INTO payment_event (
                            event_id, event_type, aggregate_no, payload, status
                        )
                        VALUES (
                            :eventId, 'PAYMENT_SUCCESS', :orderNo, CAST(:payload AS jsonb), 'PENDING'
                        )
                        """)
                .param("eventId", eventId)
                .param("orderNo", orderNo)
                .param("payload", payload)
                .update();
    }

    public void insertNotifyRecord(String notifyNo, String merchantNo, String orderNo, String callbackUrl) {
        jdbcClient.sql("""
                        INSERT INTO notify_record (
                            notify_no, merchant_no, order_no, callback_url, status, next_retry_at
                        )
                        VALUES (
                            :notifyNo, :merchantNo, :orderNo, :callbackUrl, 'PENDING', CURRENT_TIMESTAMP
                        )
                        """)
                .param("notifyNo", notifyNo)
                .param("merchantNo", merchantNo)
                .param("orderNo", orderNo)
                .param("callbackUrl", callbackUrl)
                .update();
    }

    public void markPaymentEventPublished(String eventId) {
        jdbcClient.sql("""
                        UPDATE payment_event
                        SET status = 'PUBLISHED',
                            updated_at = CURRENT_TIMESTAMP
                        WHERE event_id = :eventId
                          AND status IN ('PENDING', 'PUBLISH_FAILED')
                        """)
                .param("eventId", eventId)
                .update();
    }

    public void markPaymentEventPublishFailed(String eventId) {
        jdbcClient.sql("""
                        UPDATE payment_event
                        SET status = 'PUBLISH_FAILED',
                            retry_count = retry_count + 1,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE event_id = :eventId
                          AND status NOT IN ('DONE', 'FAILED')
                        """)
                .param("eventId", eventId)
                .update();
    }
}
