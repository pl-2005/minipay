package com.minipay.auth.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import com.minipay.auth.repository.MerchantAccountRepository;
import com.minipay.auth.repository.UserAccount;
import com.minipay.auth.repository.UserRepository;
import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.common.auth.MinipayTokenService;
import com.minipay.common.auth.TokenPrincipal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final MerchantAccountRepository merchantAccountRepository;
    private final MinipayTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            MerchantAccountRepository merchantAccountRepository,
            MinipayTokenService tokenService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.merchantAccountRepository = merchantAccountRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.username())
                .filter(account -> "ACTIVE".equals(account.status()))
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "invalid username or password");
        }

        return issueLoginResponse(user.username(), user.role());
    }

    @Transactional
    public RegisterMerchantResponse registerMerchant(RegisterMerchantRequest request) {
        String username = request.username().trim();
        String merchantName = request.merchantName().trim();
        String callbackUrl = normalizeCallbackUrl(request.callbackUrl());

        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "password must not exceed 72 bytes");
        }
        if (merchantName.length() < 2) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "merchant name must contain at least 2 characters");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "username already exists");
        }

        String merchantNo = generateMerchantNo();
        long userId = userRepository.insert(username, passwordEncoder.encode(request.password()), "MERCHANT");
        long merchantId = merchantAccountRepository.insertMerchant(merchantNo, merchantName, callbackUrl);
        merchantAccountRepository.linkUser(userId, merchantId);

        LoginResponse login = issueLoginResponse(username, "MERCHANT");
        return new RegisterMerchantResponse(
                login.token(),
                login.username(),
                login.role(),
                login.expiresAt(),
                merchantNo,
                merchantName);
    }

    private LoginResponse issueLoginResponse(String username, String role) {
        String token = tokenService.issue(username, role);
        TokenPrincipal principal = tokenService.parse(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "issued token cannot be parsed"));
        return new LoginResponse(token, username, role, principal.expiresAt());
    }

    private String normalizeCallbackUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String callbackUrl = value.trim();
        try {
            URI uri = URI.create(callbackUrl);
            String scheme = uri.getScheme();
            if (uri.getHost() == null
                    || scheme == null
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException();
            }
            return callbackUrl;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "callback URL must be a valid HTTP(S) URL");
        }
    }

    private String generateMerchantNo() {
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(Locale.ROOT);
        return "M" + suffix;
    }
}
