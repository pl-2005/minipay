package com.minipay.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import com.minipay.common.api.ErrorCode;
import com.minipay.common.auth.MinipayTokenService;
import com.minipay.common.auth.TokenPrincipal;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {
    private static final String BEARER_PREFIX = "Bearer ";

    private final MinipayTokenService tokenService;

    public AuthGatewayFilter(MinipayTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublic(request.getMethod(), path)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return writeFailure(exchange, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        Optional<TokenPrincipal> principal = tokenService.parse(authorization.substring(BEARER_PREFIX.length()));
        if (principal.isEmpty()) {
            return writeFailure(exchange, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        if (!hasAccess(principal.get(), path)) {
            return writeFailure(exchange, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
        }

        return chain.filter(withPrincipal(exchange, principal.get()));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private boolean isPublic(HttpMethod method, String path) {
        return HttpMethod.OPTIONS.equals(method)
                || path.startsWith("/actuator/")
                || path.equals("/actuator")
                || path.startsWith("/api/auth/");
    }

    private boolean hasAccess(TokenPrincipal principal, String path) {
        String role = principal.role().toUpperCase(Locale.ROOT);
        if ("ADMIN".equals(role)) {
            return path.startsWith("/api/admin/");
        }
        if ("MERCHANT".equals(role)) {
            return path.startsWith("/api/merchant/")
                    || path.startsWith("/api/orders/")
                    || path.startsWith("/api/pay/");
        }
        return false;
    }

    private ServerWebExchange withPrincipal(ServerWebExchange exchange, TokenPrincipal principal) {
        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> {
                    headers.set("X-MiniPay-Username", principal.username());
                    headers.set("X-MiniPay-Role", principal.role());
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private Mono<Void> writeFailure(ServerWebExchange exchange, HttpStatus status, ErrorCode errorCode) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":\"" + errorCode.code() + "\",\"message\":\"" + errorCode.message() + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
