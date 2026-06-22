package com.minipay.merchant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantRepository {
    private static final RowMapper<Merchant> ROW_MAPPER = (rs, rowNum) -> new Merchant(
            rs.getLong("id"),
            rs.getString("merchant_no"),
            rs.getString("merchant_name"),
            rs.getString("status"),
            rs.getString("callback_url"));

    private final JdbcClient jdbcClient;

    public MerchantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Merchant> findActiveForUser(String username, String merchantNo) {
        return jdbcClient.sql("""
                        SELECT merchant.id, merchant.merchant_no, merchant.merchant_name,
                               merchant.status, merchant.callback_url
                        FROM merchant
                        JOIN merchant_user ON merchant_user.merchant_id = merchant.id
                        JOIN app_user ON app_user.id = merchant_user.user_id
                        WHERE app_user.username = :username
                          AND app_user.status = 'ACTIVE'
                          AND merchant.merchant_no = :merchantNo
                          AND merchant.status = 'ACTIVE'
                        """)
                .param("username", username)
                .param("merchantNo", merchantNo)
                .query(ROW_MAPPER)
                .optional();
    }

    public List<Merchant> listActiveForUser(String username) {
        return jdbcClient.sql("""
                        SELECT merchant.id, merchant.merchant_no, merchant.merchant_name,
                               merchant.status, merchant.callback_url
                        FROM merchant
                        JOIN merchant_user ON merchant_user.merchant_id = merchant.id
                        JOIN app_user ON app_user.id = merchant_user.user_id
                        WHERE app_user.username = :username
                          AND app_user.status = 'ACTIVE'
                          AND merchant.status = 'ACTIVE'
                        ORDER BY merchant.merchant_no
                        LIMIT 100
                        """)
                .param("username", username)
                .query(ROW_MAPPER)
                .list();
    }
}
