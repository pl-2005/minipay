package com.minipay.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import com.minipay.common.auth.MinipayTokenService;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import reactor.core.publisher.Mono;

class AuthGatewayFilterTest {
    private static final String SECRET = "gateway-filter-test-secret";

    private final MinipayTokenService tokenService = new MinipayTokenService(SECRET, Duration.ofMinutes(5));
    private final AuthGatewayFilter filter = new AuthGatewayFilter(tokenService);

    @Test
    void adminCannotAccessMerchantApi() {
        FilterResult result = filter("ADMIN", "/api/merchant/orders");

        assertThat(result.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result.chainInvoked()).isFalse();
    }

    @Test
    void merchantCannotAccessAdminApi() {
        FilterResult result = filter("MERCHANT", "/api/admin/orders");

        assertThat(result.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result.chainInvoked()).isFalse();
    }

    @Test
    void eachRoleCanAccessItsOwnApi() {
        assertThat(filter("ADMIN", "/api/admin/orders").chainInvoked()).isTrue();
        assertThat(filter("MERCHANT", "/api/merchant/orders").chainInvoked()).isTrue();
    }

    private FilterResult filter(String role, String path) {
        String token = tokenService.issue(role.toLowerCase(), role);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        AtomicBoolean chainInvoked = new AtomicBoolean();
        GatewayFilterChain chain = ignored -> {
            chainInvoked.set(true);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();
        return new FilterResult(exchange.getResponse().getStatusCode(), chainInvoked.get());
    }

    private record FilterResult(HttpStatusCode status, boolean chainInvoked) {
    }
}
