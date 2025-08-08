package com.mgaye.banking_application.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mgaye.banking_application.entity.PasswordHistory;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.PasswordReusedException;
import com.mgaye.banking_application.exception.WeakPasswordException;
import com.mgaye.banking_application.repository.PasswordHistoryRepository;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void validatePasswordStrength_WithWeakPassword_ShouldThrowException() {
        // Given
        String weakPassword = "123";

        // When & Then
        assertThatThrownBy(() -> passwordService.validatePasswordStrength(weakPassword))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessage("Password must be at least 8 characters long");
    }

    @Test
    void validatePasswordStrength_WithoutUppercase_ShouldThrowException() {
        // Given
        String password = "password123!";

        // When & Then
        assertThatThrownBy(() -> passwordService.validatePasswordStrength(password))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessage("Password must contain at least one uppercase letter");
    }

    @Test
    void validatePasswordStrength_WithStrongPassword_ShouldPass() {
        // Given
        String strongPassword = "StrongPass123!";

        // When & Then
        assertThatCode(() -> passwordService.validatePasswordStrength(strongPassword))
                .doesNotThrowAnyException();
    }

    @Test
    void validatePasswordHistory_WithReusedPassword_ShouldThrowException() {
        // Given
        User user = new User();
        String newPassword = "NewPass123!";
        String hashedPassword = "hashed-password";

        PasswordHistory history = new PasswordHistory();
        history.setPasswordHash(hashedPassword);

        when(passwordHistoryRepository.findTop5ByUserOrderByChangedAtDesc(user))
                .thenReturn(List.of(history));
        when(passwordEncoder.matches(newPassword, hashedPassword)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> passwordService.validatePasswordHistory(user, newPassword))
                .isInstanceOf(PasswordReusedException.class)
                .hasMessage("Password was used recently. Please choose a different password");
    }
}