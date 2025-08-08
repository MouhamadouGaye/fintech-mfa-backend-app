package com.mgaye.banking_application.service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TotpService {

    private static final String SECRET_KEY_ALGORITHM = "HmacSHA1";
    private static final int SECRET_KEY_LENGTH = 20; // 160 bits

    public String generateSecret() {
        byte[] secretKey = new byte[SECRET_KEY_LENGTH];
        new SecureRandom().nextBytes(secretKey);
        return new Base32().encodeToString(secretKey);
    }

    public boolean verifyTotp(String secret, String code, long timeWindow) {
        try {
            long currentTimeStep = System.currentTimeMillis() / 30000; // 30-second window

            // Check current window and ±1 window for clock skew
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTotpCode(secret, currentTimeStep + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
            return false;
        }
    }

    private String generateTotpCode(String secret, long timeStep) throws Exception {
        byte[] secretBytes = new Base32().decode(secret);
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(timeStep).array();

        Mac mac = Mac.getInstance(SECRET_KEY_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, SECRET_KEY_ALGORITHM);
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(timeBytes);
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }

    public String getQrCodeUrl(String username, String issuer, String secret) {
        String url = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, username, secret, issuer);
        return url;
    }
}