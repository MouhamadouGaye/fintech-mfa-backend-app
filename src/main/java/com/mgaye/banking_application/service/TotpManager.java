package com.mgaye.banking_application.service;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TotpManager {

    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int SECRET_LENGTH = 20; // 160 bits
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int DIGITS = 6;
    private static final String ISSUER = "YourApp";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a new secret key for TOTP
     */
    public String generateSecret() {
        byte[] secret = new byte[SECRET_LENGTH];
        secureRandom.nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    /**
     * Generates QR code URL for Google Authenticator
     */
    public String getQrCodeUrl(String userEmail, String secret) {
        try {
            String encodedIssuer = URLEncoder.encode(ISSUER, StandardCharsets.UTF_8.toString());
            String encodedUserEmail = URLEncoder.encode(userEmail, StandardCharsets.UTF_8.toString());

            String otpAuthUrl = String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                    encodedIssuer,
                    encodedUserEmail,
                    secret.replace("=", ""), // Remove padding
                    encodedIssuer,
                    DIGITS,
                    TIME_STEP);

            return String.format(
                    "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=%s",
                    URLEncoder.encode(otpAuthUrl, StandardCharsets.UTF_8.toString()));
        } catch (Exception e) {
            log.error("Error generating QR code URL", e);
            throw new RuntimeException("Failed to generate QR code URL", e);
        }
    }

    /**
     * Verifies a TOTP code
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        try {
            long currentTime = System.currentTimeMillis() / 1000L;
            long timeWindow = currentTime / TIME_STEP;

            // Check current window and one window before/after to account for clock skew
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateCode(secret, timeWindow + i);
                if (constantTimeEquals(code, expectedCode)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
            return false;
        }
    }

    /**
     * Generates TOTP code for current time
     */
    public String generateCurrentCode(String secret) {
        long currentTime = System.currentTimeMillis() / 1000L;
        long timeWindow = currentTime / TIME_STEP;
        return generateCode(secret, timeWindow);
    }

    /**
     * Generates TOTP code for specific time window
     */
    private String generateCode(String secret, long timeWindow) {
        try {
            byte[] secretBytes = Base64.getDecoder().decode(secret);
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeWindow).array();

            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(timeBytes);

            // Dynamic truncation
            int offset = hash[hash.length - 1] & 0x0F;
            int code = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            code = code % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", code);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating TOTP code", e);
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    /**
     * Constant time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Validates secret format
     */
    public boolean isValidSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            return false;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            return decoded.length >= 16; // At least 128 bits
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets remaining time in current window
     */
    public int getRemainingTimeInWindow() {
        long currentTime = System.currentTimeMillis() / 1000L;
        return TIME_STEP - (int) (currentTime % TIME_STEP);
    }
}
