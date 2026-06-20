package com.minipay.common.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MinipayTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final Duration ttl;
    private final Clock clock;

    public MinipayTokenService(String secret, Duration ttl) {
        this(secret, ttl, Clock.systemUTC());
    }

    MinipayTokenService(String secret, Duration ttl, Clock clock) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("token secret must not be blank");
        }
        this.secret = secret;
        this.ttl = ttl;
        this.clock = clock;
    }

    public String issue(String username, String role) {
        long expiresAt = Instant.now(clock).plus(ttl).getEpochSecond();
        String payload = encode(username) + "." + encode(role) + "." + expiresAt + "." + encode(UUID.randomUUID().toString());
        return payload + "." + sign(payload);
    }

    public Optional<TokenPrincipal> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 5) {
            return Optional.empty();
        }

        String payload = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), parts[4].getBytes(StandardCharsets.UTF_8))) {
            return Optional.empty();
        }

        long expiresAt;
        try {
            expiresAt = Long.parseLong(parts[2]);
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }

        if (Instant.now(clock).getEpochSecond() > expiresAt) {
            return Optional.empty();
        }

        try {
            return Optional.of(new TokenPrincipal(decode(parts[0]), decode(parts[1]), expiresAt));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("failed to sign token", exception);
        }
    }

    private String encode(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(DECODER.decode(value), StandardCharsets.UTF_8);
    }
}
