package com.minipay.auth.service;

import com.minipay.auth.repository.UserAccount;
import com.minipay.auth.repository.UserRepository;
import com.minipay.common.api.BusinessException;
import com.minipay.common.api.ErrorCode;
import com.minipay.common.auth.MinipayTokenService;
import com.minipay.common.auth.TokenPrincipal;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final MinipayTokenService tokenService;

    public AuthService(UserRepository userRepository, MinipayTokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.username())
                .filter(account -> "ACTIVE".equals(account.status()))
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "invalid username or password"));

        if (!matches(request.password(), user.passwordHash())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "invalid username or password");
        }

        String token = tokenService.issue(user.username(), user.role());
        TokenPrincipal principal = tokenService.parse(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "issued token cannot be parsed"));
        return new LoginResponse(token, user.username(), user.role(), principal.expiresAt());
    }

    private boolean matches(String password, String passwordHash) {
        if (passwordHash == null) {
            return false;
        }
        if (passwordHash.startsWith("{noop}")) {
            return passwordHash.substring("{noop}".length()).equals(password);
        }
        return passwordHash.equals(password);
    }
}
