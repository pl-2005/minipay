package com.minipay.auth.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantAccountRepository {
    private final JdbcClient jdbcClient;

    public MerchantAccountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long insertMerchant(String merchantNo, String merchantName, String callbackUrl) {
        return jdbcClient.sql("""
                        INSERT INTO merchant (merchant_no, merchant_name, callback_url)
                        VALUES (:merchantNo, :merchantName, :callbackUrl)
                        RETURNING id
                        """)
                .param("merchantNo", merchantNo)
                .param("merchantName", merchantName)
                .param("callbackUrl", callbackUrl)
                .query(Long.class)
                .single();
    }

    public void linkUser(long userId, long merchantId) {
        jdbcClient.sql("""
                        INSERT INTO merchant_user (user_id, merchant_id)
                        VALUES (:userId, :merchantId)
                        """)
                .param("userId", userId)
                .param("merchantId", merchantId)
                .update();
    }
}
