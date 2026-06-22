package com.minipay.admin.repository;

import java.util.List;
import java.util.Locale;

import com.minipay.admin.service.AdminEventResponse;
import com.minipay.admin.service.AdminNotificationResponse;
import com.minipay.admin.service.AdminOrderResponse;
import com.minipay.admin.service.AdminPaymentResponse;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {
    private static final RowMapper<AdminOrderResponse> ORDER_ROW_MAPPER = (rs, rowNum) -> new AdminOrderResponse(
            rs.getString("order_no"),
            rs.getString("merchant_no"),
            rs.getString("merchant_order_no"),
            rs.getString("subject"),
            rs.getBigDecimal("amount"),
            rs.getString("status"),
            rs.getTimestamp("expire_at") == null ? null : rs.getTimestamp("expire_at").toLocalDateTime(),
            rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime());

    private static final RowMapper<AdminPaymentResponse> PAYMENT_ROW_MAPPER = (rs, rowNum) -> new AdminPaymentResponse(
            rs.getString("payment_no"),
            rs.getString("order_no"),
            rs.getString("merchant_no"),
            rs.getBigDecimal("amount"),
            rs.getString("status"),
            rs.getString("idempotency_key"),
            rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime(),
            rs.getTimestamp("created_at").toLocalDateTime());

    private static final RowMapper<AdminEventResponse> EVENT_ROW_MAPPER = (rs, rowNum) -> new AdminEventResponse(
            rs.getString("event_id"),
            rs.getString("event_type"),
            rs.getString("aggregate_no"),
            rs.getString("payload"),
            rs.getString("status"),
            rs.getInt("retry_count"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private static final RowMapper<AdminNotificationResponse> NOTIFICATION_ROW_MAPPER =
            (rs, rowNum) -> new AdminNotificationResponse(
                    rs.getString("notify_no"),
                    rs.getString("merchant_no"),
                    rs.getString("order_no"),
                    rs.getString("callback_url"),
                    rs.getString("status"),
                    rs.getString("response_body"),
                    rs.getInt("retry_count"),
                    rs.getTimestamp("next_retry_at") == null
                            ? null
                            : rs.getTimestamp("next_retry_at").toLocalDateTime(),
                    rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcClient jdbcClient;

    public AdminRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AdminOrderResponse> listOrders(String keyword, String status) {
        SearchParameters parameters = searchParameters(keyword, status);
        return jdbcClient.sql("""
                        SELECT order_no, merchant_no, merchant_order_no, subject, amount, status,
                               expire_at, paid_at, created_at
                        FROM pay_order
                        WHERE (:status = '' OR status = :status)
                          AND (
                              :keyword = ''
                              OR order_no ILIKE :keywordPattern
                              OR merchant_no ILIKE :keywordPattern
                              OR merchant_order_no ILIKE :keywordPattern
                              OR subject ILIKE :keywordPattern
                          )
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .param("keyword", parameters.keyword())
                .param("keywordPattern", parameters.keywordPattern())
                .param("status", parameters.status())
                .query(ORDER_ROW_MAPPER)
                .list();
    }

    public List<AdminPaymentResponse> listPayments(String keyword, String status) {
        SearchParameters parameters = searchParameters(keyword, status);
        return jdbcClient.sql("""
                        SELECT payment_no, order_no, merchant_no, amount, status, idempotency_key,
                               paid_at, created_at
                        FROM payment_record
                        WHERE (:status = '' OR status = :status)
                          AND (
                              :keyword = ''
                              OR payment_no ILIKE :keywordPattern
                              OR order_no ILIKE :keywordPattern
                              OR merchant_no ILIKE :keywordPattern
                              OR idempotency_key ILIKE :keywordPattern
                          )
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .param("keyword", parameters.keyword())
                .param("keywordPattern", parameters.keywordPattern())
                .param("status", parameters.status())
                .query(PAYMENT_ROW_MAPPER)
                .list();
    }

    public List<AdminEventResponse> listEvents(String keyword, String eventType, String status) {
        SearchParameters parameters = searchParameters(keyword, status);
        String normalizedEventType = normalize(eventType).toUpperCase(Locale.ROOT);
        return jdbcClient.sql("""
                        SELECT event_id, event_type, aggregate_no, payload, status, retry_count, created_at
                        FROM payment_event
                        WHERE (:status = '' OR status = :status)
                          AND (:eventType = '' OR event_type = :eventType)
                          AND (
                              :keyword = ''
                              OR event_id ILIKE :keywordPattern
                              OR aggregate_no ILIKE :keywordPattern
                              OR CAST(payload AS TEXT) ILIKE :keywordPattern
                          )
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .param("keyword", parameters.keyword())
                .param("keywordPattern", parameters.keywordPattern())
                .param("eventType", normalizedEventType)
                .param("status", parameters.status())
                .query(EVENT_ROW_MAPPER)
                .list();
    }

    public List<AdminNotificationResponse> listNotifications(String keyword, String status) {
        SearchParameters parameters = searchParameters(keyword, status);
        return jdbcClient.sql("""
                        SELECT notify_no, merchant_no, order_no, callback_url, status,
                               response_body, retry_count, next_retry_at, created_at
                        FROM notify_record
                        WHERE (:status = '' OR status = :status)
                          AND (
                              :keyword = ''
                              OR notify_no ILIKE :keywordPattern
                              OR merchant_no ILIKE :keywordPattern
                              OR order_no ILIKE :keywordPattern
                              OR callback_url ILIKE :keywordPattern
                              OR COALESCE(response_body, '') ILIKE :keywordPattern
                          )
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .param("keyword", parameters.keyword())
                .param("keywordPattern", parameters.keywordPattern())
                .param("status", parameters.status())
                .query(NOTIFICATION_ROW_MAPPER)
                .list();
    }

    private SearchParameters searchParameters(String keyword, String status) {
        String normalizedKeyword = normalize(keyword);
        return new SearchParameters(
                normalizedKeyword,
                "%" + normalizedKeyword + "%",
                normalize(status).toUpperCase(Locale.ROOT));
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private record SearchParameters(String keyword, String keywordPattern, String status) {
    }
}
