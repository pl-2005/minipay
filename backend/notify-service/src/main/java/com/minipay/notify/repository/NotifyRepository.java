package com.minipay.notify.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class NotifyRepository {
    private final JdbcClient jdbcClient;

    private static final RowMapper<PaymentEvent> EVENT_ROW_MAPPER = (rs, rowNum) -> new PaymentEvent(
            rs.getLong("id"),
            rs.getString("event_id"),
            rs.getString("event_type"),
            rs.getString("aggregate_no"),
            rs.getString("payload"),
            rs.getString("status"),
            rs.getInt("retry_count"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime());

    private static final RowMapper<NotifyRecord> NOTIFY_ROW_MAPPER = (rs, rowNum) -> new NotifyRecord(
            rs.getLong("id"),
            rs.getString("notify_no"),
            rs.getString("merchant_no"),
            rs.getString("order_no"),
            rs.getString("callback_url"),
            rs.getString("status"),
            rs.getString("response_body"),
            rs.getInt("retry_count"),
            rs.getTimestamp("next_retry_at") == null ? null : rs.getTimestamp("next_retry_at").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime());

    public NotifyRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PaymentEvent> findEventByEventId(String eventId) {
        return jdbcClient.sql("""
                        SELECT id, event_id, event_type, aggregate_no, payload, status,
                               retry_count, created_at, updated_at
                        FROM payment_event
                        WHERE event_id = :eventId
                        """)
                .param("eventId", eventId)
                .query(EVENT_ROW_MAPPER)
                .optional();
    }

    public Optional<PaymentEvent> findLatestEventByOrderNo(String orderNo) {
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

    public Optional<NotifyRecord> findLatestNotifyByOrderNo(String orderNo) {
        return jdbcClient.sql("""
                        SELECT id, notify_no, merchant_no, order_no, callback_url, status,
                               response_body, retry_count, next_retry_at, created_at, updated_at
                        FROM notify_record
                        WHERE order_no = :orderNo
                        ORDER BY created_at DESC
                        LIMIT 1
                        """)
                .param("orderNo", orderNo)
                .query(NOTIFY_ROW_MAPPER)
                .optional();
    }

    public List<NotifyRecord> findDueNotifications(int limit) {
        return jdbcClient.sql("""
                        SELECT id, notify_no, merchant_no, order_no, callback_url, status,
                               response_body, retry_count, next_retry_at, created_at, updated_at
                        FROM notify_record
                        WHERE (
                            status = 'RETRY'
                            AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP)
                        ) OR (
                            status = 'PROCESSING'
                            AND updated_at <= CURRENT_TIMESTAMP - INTERVAL '2 minutes'
                        )
                        ORDER BY updated_at ASC
                        LIMIT :limit
                        """)
                .param("limit", limit)
                .query(NOTIFY_ROW_MAPPER)
                .list();
    }

    public boolean claimNotification(String notifyNo) {
        int updated = jdbcClient.sql("""
                        UPDATE notify_record
                        SET status = 'PROCESSING',
                            updated_at = CURRENT_TIMESTAMP
                        WHERE notify_no = :notifyNo
                          AND (
                              status IN ('PENDING', 'RETRY')
                              OR (
                                  status = 'PROCESSING'
                                  AND updated_at <= CURRENT_TIMESTAMP - INTERVAL '2 minutes'
                              )
                          )
                          AND (next_retry_at IS NULL OR next_retry_at <= CURRENT_TIMESTAMP OR status = 'PROCESSING')
                        """)
                .param("notifyNo", notifyNo)
                .update();
        return updated == 1;
    }

    public void markNotifySuccess(String notifyNo, String responseBody) {
        jdbcClient.sql("""
                        UPDATE notify_record
                        SET status = 'SUCCESS',
                            response_body = :responseBody,
                            next_retry_at = NULL,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE notify_no = :notifyNo
                        """)
                .param("notifyNo", notifyNo)
                .param("responseBody", responseBody)
                .update();
    }

    public void markNotifyRetry(String notifyNo, String responseBody, LocalDateTime nextRetryAt) {
        jdbcClient.sql("""
                        UPDATE notify_record
                        SET status = 'RETRY',
                            response_body = :responseBody,
                            retry_count = retry_count + 1,
                            next_retry_at = :nextRetryAt,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE notify_no = :notifyNo
                        """)
                .param("notifyNo", notifyNo)
                .param("responseBody", responseBody)
                .param("nextRetryAt", nextRetryAt)
                .update();
    }

    public void markNotifyFailed(String notifyNo, String responseBody) {
        jdbcClient.sql("""
                        UPDATE notify_record
                        SET status = 'FAILED',
                            response_body = :responseBody,
                            retry_count = retry_count + 1,
                            next_retry_at = NULL,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE notify_no = :notifyNo
                        """)
                .param("notifyNo", notifyNo)
                .param("responseBody", responseBody)
                .update();
    }

    public void markEventDone(String eventId) {
        jdbcClient.sql("""
                        UPDATE payment_event
                        SET status = 'DONE',
                            updated_at = CURRENT_TIMESTAMP
                        WHERE event_id = :eventId
                        """)
                .param("eventId", eventId)
                .update();
    }

    public void markEventRetry(String eventId) {
        jdbcClient.sql("""
                        UPDATE payment_event
                        SET status = 'RETRY',
                            retry_count = retry_count + 1,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE event_id = :eventId AND status <> 'DONE'
                        """)
                .param("eventId", eventId)
                .update();
    }

    public void markEventFailed(String eventId) {
        jdbcClient.sql("""
                        UPDATE payment_event
                        SET status = 'FAILED',
                            retry_count = retry_count + 1,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE event_id = :eventId AND status <> 'DONE'
                        """)
                .param("eventId", eventId)
                .update();
    }
}
