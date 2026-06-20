package com.minipay.merchant.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    public Optional<PayOrder> findByMerchantOrderNo(String merchantNo, String merchantOrderNo) {
        return jdbcClient.sql("""
                        SELECT id, order_no, merchant_no, merchant_order_no, subject, amount, status,
                               callback_url, expire_at, paid_at, created_at, updated_at
                        FROM pay_order
                        WHERE merchant_no = :merchantNo AND merchant_order_no = :merchantOrderNo
                        """)
                .param("merchantNo", merchantNo)
                .param("merchantOrderNo", merchantOrderNo)
                .query(ROW_MAPPER)
                .optional();
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

    public List<PayOrder> listByMerchant(String merchantNo, String status) {
        if (status == null || status.isBlank()) {
            return jdbcClient.sql("""
                            SELECT id, order_no, merchant_no, merchant_order_no, subject, amount, status,
                                   callback_url, expire_at, paid_at, created_at, updated_at
                            FROM pay_order
                            WHERE merchant_no = :merchantNo
                            ORDER BY created_at DESC
                            LIMIT 100
                            """)
                    .param("merchantNo", merchantNo)
                    .query(ROW_MAPPER)
                    .list();
        }

        return jdbcClient.sql("""
                        SELECT id, order_no, merchant_no, merchant_order_no, subject, amount, status,
                               callback_url, expire_at, paid_at, created_at, updated_at
                        FROM pay_order
                        WHERE merchant_no = :merchantNo AND status = :status
                        ORDER BY created_at DESC
                        LIMIT 100
                        """)
                .param("merchantNo", merchantNo)
                .param("status", status)
                .query(ROW_MAPPER)
                .list();
    }

    public void insert(
            String orderNo,
            String merchantNo,
            String merchantOrderNo,
            String subject,
            BigDecimal amount,
            String callbackUrl,
            LocalDateTime expireAt) {
        jdbcClient.sql("""
                        INSERT INTO pay_order (
                            order_no, merchant_no, merchant_order_no, subject, amount,
                            status, callback_url, expire_at
                        )
                        VALUES (
                            :orderNo, :merchantNo, :merchantOrderNo, :subject, :amount,
                            'PENDING', :callbackUrl, :expireAt
                        )
                        """)
                .param("orderNo", orderNo)
                .param("merchantNo", merchantNo)
                .param("merchantOrderNo", merchantOrderNo)
                .param("subject", subject)
                .param("amount", amount)
                .param("callbackUrl", callbackUrl)
                .param("expireAt", expireAt)
                .update();
    }
}
