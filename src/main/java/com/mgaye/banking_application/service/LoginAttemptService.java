package com.mgaye.banking_application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.mgaye.banking_application.entity.LoginAttempt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    public void recordAttempt(String email, String ipAddress, String userAgent, boolean successful, String details) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(email)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .success(successful)
                .attemptTime(LocalDateTime.now())
                .details(details)
                .build();

        // Store in Redis for rate limiting
        String key = LOGIN_ATTEMPT_PREFIX + email + ":" + ipAddress;
        String attemptJson = convertToJson(attempt);

        redisTemplate.opsForValue().set(key, attemptJson, LOCKOUT_DURATION);

        // Log the attempt
        if (successful) {
            log.info("Successful login attempt for email: {} from IP: {}", email, ipAddress);
        } else {
            log.warn("Failed login attempt for email: {} from IP: {} - {}", email, ipAddress, details);
        }
    }

    public boolean isAccountLocked(String email, String ipAddress) {
        String key = LOGIN_ATTEMPT_PREFIX + email + ":" + ipAddress;
        String value = redisTemplate.opsForValue().get(key);

        if (value != null) {
            LoginAttempt attempt = convertFromJson(value);
            return !attempt.getSuccess();
        }

        return false;
    }

    public int getFailedAttemptCount(String email, String ipAddress) {
        // This is a simplified version - in production you might want to track multiple
        // attempts
        String key = LOGIN_ATTEMPT_PREFIX + email + ":" + ipAddress;
        return redisTemplate.hasKey(key) ? 1 : 0;
    }

    public void clearFailedAttempts(String email, String ipAddress) {
        String key = LOGIN_ATTEMPT_PREFIX + email + ":" + ipAddress;
        redisTemplate.delete(key);
    }

    private String convertToJson(LoginAttempt attempt) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.format(
                "{\"email\":\"%s\",\"ipAddress\":\"%s\",\"userAgent\":\"%s\",\"successful\":%b,\"timestamp\":\"%s\",\"details\":\"%s\"}",
                attempt.getEmail(),
                attempt.getIpAddress(),
                attempt.getUserAgent(),
                attempt.getSuccess(), // Must be boolean
                attempt.getAttemptTime().format(formatter),
                attempt.getDetails());
    }

    private LoginAttempt convertFromJson(String json) {
        // Simple JSON parsing - you might want to use ObjectMapper
        // This is a simplified implementation
        return LoginAttempt.builder()
                .success(json.contains("\"successful\":true"))
                .attemptTime(LocalDateTime.now()) // Simplified
                .build();
    }
}
