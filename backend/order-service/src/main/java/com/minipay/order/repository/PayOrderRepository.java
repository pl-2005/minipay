package com.minipay.order.repository;

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

    public Optional<PayOrder> findByOrderNoForUser(String username, String orderNo) {
        return jdbcClient.sql("""
                        SELECT pay_order.id, pay_order.order_no, pay_order.merchant_no,
                               pay_order.merchant_order_no, pay_order.subject, pay_order.amount,
                               pay_order.status, pay_order.callback_url, pay_order.expire_at,
                               pay_order.paid_at, pay_order.created_at, pay_order.updated_at
                        FROM pay_order
                        JOIN merchant ON merchant.merchant_no = pay_order.merchant_no
                        JOIN merchant_user ON merchant_user.merchant_id = merchant.id
                        JOIN app_user ON app_user.id = merchant_user.user_id
                        WHERE app_user.username = :username
                          AND app_user.status = 'ACTIVE'
                          AND merchant.status = 'ACTIVE'
                          AND pay_order.order_no = :orderNo
                        """)
                .param("username", username)
                .param("orderNo", orderNo)
                .query(ROW_MAPPER)
                .optional();
    }
}
