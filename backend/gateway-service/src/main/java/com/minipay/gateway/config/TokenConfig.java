package com.minipay.gateway.config;

import java.time.Duration;

import com.minipay.common.auth.MinipayTokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenConfig {
    @Bean
    MinipayTokenService minipayTokenService(
            @Value("${minipay.auth.secret:minipay-dev-secret-change-me}") String secret,
            @Value("${minipay.auth.token-ttl-minutes:480}") long tokenTtlMinutes) {
        return new MinipayTokenService(secret, Duration.ofMinutes(tokenTtlMinutes));
    }
}
