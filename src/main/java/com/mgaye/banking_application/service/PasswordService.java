package com.mgaye.banking_application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mgaye.banking_application.entity.PasswordHistory;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.PasswordReusedException;
import com.mgaye.banking_application.exception.WeakPasswordException;
import com.mgaye.banking_application.repository.PasswordHistoryRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public PasswordService(PasswordEncoder passwordEncoder, PasswordHistoryRepository passwordHistoryRepository) {
        this.passwordEncoder = passwordEncoder;
        this.passwordHistoryRepository = passwordHistoryRepository;
    }

    public void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new WeakPasswordException("Password must contain atwx least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new WeakPasswordException("Password must contain at least one digit");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*")) {
            throw new WeakPasswordException("Password must contain at least one special character");
        }

        // Check for common passwords
        if (isCommonPassword(password)) {
            throw new WeakPasswordException("Password is too common. Please choose a stronger password");
        }
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public void validatePasswordHistory(User user, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository
                .findTop5ByUserOrderByChangedAtDesc(user);

        for (PasswordHistory hist : history) {
            if (passwordEncoder.matches(newPassword, hist.getPasswordHash())) {
                throw new PasswordReusedException("Password was used recently. Please choose a different password");
            }
        }
    }

    public void savePasswordHistory(User user, String oldPasswordHash, String reason, String ipAddress) {
        PasswordHistory history = new PasswordHistory();
        history.setUser(user);
        history.setPasswordHash(oldPasswordHash);
        history.setChangedAt(LocalDateTime.now());
        history.setChangeReason(reason);
        history.setChangedByIp(ipAddress);

        passwordHistoryRepository.save(history);
    }

    private boolean isCommonPassword(String password) {
        Set<String> commonPasswords = Set.of(
                "password", "123456", "12345678", "qwerty", "abc123",
                "password123", "admin", "letmein", "welcome", "monkey");
        return commonPasswords.contains(password.toLowerCase());
    }

}
