package com.minipay.auth.repository;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<UserAccount> findByUsername(String username) {
        return jdbcClient.sql("""
                        SELECT id, username, password_hash, role, status
                        FROM app_user
                        WHERE username = :username
                        """)
                .param("username", username)
                .query((rs, rowNum) -> new UserAccount(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("status")))
                .optional();
    }

    public boolean existsByUsername(String username) {
        return jdbcClient.sql("SELECT EXISTS(SELECT 1 FROM app_user WHERE username = :username)")
                .param("username", username)
                .query(Boolean.class)
                .single();
    }

    public long insert(String username, String passwordHash, String role) {
        return jdbcClient.sql("""
                        INSERT INTO app_user (username, password_hash, role)
                        VALUES (:username, :passwordHash, :role)
                        RETURNING id
                        """)
                .param("username", username)
                .param("passwordHash", passwordHash)
                .param("role", role)
                .query(Long.class)
                .single();
    }
}
