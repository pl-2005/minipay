package com.minipay.merchant.repository;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MerchantRepository {
    private final JdbcClient jdbcClient;

    public MerchantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Merchant> findActiveByMerchantNo(String merchantNo) {
        return jdbcClient.sql("""
                        SELECT id, merchant_no, merchant_name, status, callback_url
                        FROM merchant
                        WHERE merchant_no = :merchantNo AND status = 'ACTIVE'
                        """)
                .param("merchantNo", merchantNo)
                .query((rs, rowNum) -> new Merchant(
                        rs.getLong("id"),
                        rs.getString("merchant_no"),
                        rs.getString("merchant_name"),
                        rs.getString("status"),
                        rs.getString("callback_url")))
                .optional();
    }
}
