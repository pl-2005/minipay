package com.minipay.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;

import com.minipay.auth.repository.MerchantAccountRepository;
import com.minipay.auth.repository.UserRepository;
import com.minipay.common.api.BusinessException;
import com.minipay.common.auth.MinipayTokenService;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MerchantAccountRepository merchantAccountRepository = mock(MerchantAccountRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final MinipayTokenService tokenService = new MinipayTokenService(
            "auth-service-test-secret",
            Duration.ofMinutes(5));
    private final AuthService authService = new AuthService(
            userRepository,
            merchantAccountRepository,
            tokenService,
            passwordEncoder);

    @Test
    void registersMerchantAccountAndIssuesToken() {
        when(userRepository.existsByUsername("new-merchant")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("{bcrypt}encoded");
        when(userRepository.insert("new-merchant", "{bcrypt}encoded", "MERCHANT")).thenReturn(11L);
        when(merchantAccountRepository.insertMerchant(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq("New Merchant"),
                org.mockito.ArgumentMatchers.eq("https://merchant.example.com/callback")))
                .thenReturn(21L);

        RegisterMerchantResponse response = authService.registerMerchant(new RegisterMerchantRequest(
                "new-merchant",
                "password123",
                "New Merchant",
                "https://merchant.example.com/callback"));

        assertThat(response.username()).isEqualTo("new-merchant");
        assertThat(response.role()).isEqualTo("MERCHANT");
        assertThat(response.merchantNo()).startsWith("M").hasSize(13);
        assertThat(tokenService.parse(response.token())).isPresent();
        verify(merchantAccountRepository).linkUser(11L, 21L);
    }

    @Test
    void rejectsDuplicateUsernameBeforeWritingData() {
        when(userRepository.existsByUsername("existing-user")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerMerchant(new RegisterMerchantRequest(
                "existing-user",
                "password123",
                "Existing Merchant",
                null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("username already exists");

        verifyNoInteractions(merchantAccountRepository);
    }

    @Test
    void rejectsInvalidCallbackUrl() {
        assertThatThrownBy(() -> authService.registerMerchant(new RegisterMerchantRequest(
                "new-merchant",
                "password123",
                "New Merchant",
                "ftp://merchant.example.com/callback")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("callback URL must be a valid HTTP(S) URL");

        verifyNoInteractions(merchantAccountRepository);
    }

    @Test
    void rejectsPasswordBeyondBcryptByteLimit() {
        assertThatThrownBy(() -> authService.registerMerchant(new RegisterMerchantRequest(
                "new-merchant",
                "a".repeat(73),
                "New Merchant",
                null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("password must not exceed 72 bytes");

        verifyNoInteractions(merchantAccountRepository);
    }
}
