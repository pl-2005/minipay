package com.minipay.order.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PayOrderRepository {
    private final JdbcClient jdbcClient;
    private static final RowMapper<PayOrder> ROW_MAPPER = (rs, rowNum) -> new PayOrder(
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

    public PayOrderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<PayOrder> findByOrderNo(String orderNo) {
        return jdbcClient.sql("""
                        SELECT id, order_no, merchant_no, merchant_order_no, subject, amount, status,
                               callback_url, expire_at, paid_at, created_at, updated_at
                        FROM pay_order
                        WHERE order_no = :orderNo
                        """)
                .param("orderNo", orderNo)
                .query(ROW_MAPPER)
                .optional();
    }

    public void updateStatus(String orderNo, String targetStatus, LocalDateTime paidAt) {
        jdbcClient.sql("""
                        UPDATE pay_order
                        SET status = :status, paid_at = :paidAt, updated_at = NOW()
                        WHERE order_no = :orderNo
                        """)
                .param("status", targetStatus)
                .param("paidAt", paidAt)
                .param("orderNo", orderNo)
                .update();
    }
}