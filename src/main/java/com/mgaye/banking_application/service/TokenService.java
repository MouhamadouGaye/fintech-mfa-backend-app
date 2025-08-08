// package com.mgaye.banking_application.service;

// import java.security.SecureRandom;
// import java.util.Base64;

// import org.springframework.stereotype.Service;

// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class TokenService {

//     private final SecureRandom secureRandom = new SecureRandom();

//     public String generateVerificationToken() {
//         return generateSecureToken(32);
//     }

//     public String generatePasswordResetToken() {
//         return generateSecureToken(32);
//     }

//     public String generateMfaSecret() {
//         byte[] secretBytes = new byte[20];
//         secureRandom.nextBytes(secretBytes);
//         return Base32.encode(secretBytes);
//     }

//     private String generateSecureToken(int length) {
//         byte[] tokenBytes = new byte[length];
//         secureRandom.nextBytes(tokenBytes);
//         return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
//     }
// }

package com.mgaye.banking_application.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class TokenService {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a verification token for email verification
     */
    public String generateVerificationToken(Object user) {
        // Generate a secure random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        log.debug("Generated verification token for user");
        return token;
    }

    /**
     * Generates a password reset token
     */
    public String generatePasswordResetToken(Object user) {
        // Generate a secure random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        log.debug("Generated password reset token for user");
        return token;
    }

    /**
     * Generates a simple UUID-based token
     */
    public String generateSimpleToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generates an OTP (One-Time Password) for verification
     */
    public String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Generates a 6-digit OTP
     */
    public String generateSixDigitOTP() {
        return generateOTP(6);
    }

    /**
     * Validates if a token has expired
     */
    public boolean isTokenExpired(LocalDateTime expiryTime) {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Generates a token with specified length
     */
    public String generateTokenWithLength(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generates a refresh token
     */
    public String generateRefreshToken() {
        return generateTokenWithLength(64);
    }

    /**
     * Validates token format (basic validation)
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // Check if it's a valid Base64 URL-safe string
        try {
            Base64.getUrlDecoder().decode(token);
            return true;
        } catch (IllegalArgumentException e) {
            // Try UUID format
            try {
                UUID.fromString(token);
                return true;
            } catch (IllegalArgumentException ex) {
                // Check if it's numeric (OTP)
                return token.matches("\\d+");
            }
        }
    }
}