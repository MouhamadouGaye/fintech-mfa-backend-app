package com.mgaye.banking_application.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SmsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${app.sms.provider:mock}")
    private String smsProvider;
    
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;
    
    // Phone number validation pattern (international format)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");
    
    // Code expiration time
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);
    
    public SmsService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Sends MFA code via SMS
     */
    public void sendMfaCode(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        String code = generateSixDigitCode();
        String key = "mfa_sms:" + phoneNumber;
        
        // Store code in Redis with expiration
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION);
        
        // Send SMS asynchronously
        CompletableFuture.runAsync(() -> sendSms(phoneNumber, createMfaMessage(code)));
        
        log.info("MFA code sent to phone number: {}", maskPhoneNumber(phoneNumber));
    }

    /**
     * Sends verification code via SMS
     */
    public void sendVerificationCode(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        String code = generateSixDigitCode();
        String key = "verification_sms:" + phoneNumber;
        
        // Store code in Redis with expiration
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION);
        
        // Send SMS asynchronously
        CompletableFuture.runAsync(() -> sendSms(phoneNumber, createVerificationMessage(code)));
        
        log.info("Verification code sent to phone number: {}", maskPhoneNumber(phoneNumber));
    }

    /**
     * Verifies MFA code
     */
    public boolean verifyCode(String phoneNumber, String code) {
        if (!isValidPhoneNumber(phoneNumber) || code == null || code.trim().isEmpty()) {
            return false;
        }

        String key = "mfa_sms:" + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && constantTimeEquals(code.trim(), storedCode)) {
            // Delete the code after successful verification
            redisTemplate.delete(key);
            log.info("MFA code verified successfully for: {}", maskPhoneNumber(phoneNumber));
            return true;
        }
        
        log.warn("Invalid MFA code attempt for: {}", maskPhoneNumber(phoneNumber));
        return false;
    }

    /**
     * Verifies phone verification code
     */
    public boolean verifyVerificationCode(String phoneNumber, String code) {
        if (!isValidPhoneNumber(phoneNumber) || code == null || code.trim().isEmpty()) {
            return false;
        }

        String key = "verification_sms:" + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && constantTimeEquals(code.trim(), storedCode)) {
            // Delete the code after successful verification
            redisTemplate.delete(key);
            log.info("Verification code verified successfully for: {}", maskPhoneNumber(phoneNumber));
            return true;
        }
        
        log.warn("Invalid verification code attempt for: {}", maskPhoneNumber(phoneNumber));
        return false;
    }

    /**
     * Sends custom SMS message
     */
    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS disabled - would send to {}: {}", maskPhoneNumber(phoneNumber), message);
            return;
        }

        try {
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    sendViaTwilio(phoneNumber, message);
                    break;
                case "aws":
                    sendViaAWSSNS(phoneNumber, message);
                    break;
                case "mock":
                default:
                    sendViaMock(phoneNumber, message);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    /**
     * Checks if phone number has pending verification
     */
    public boolean hasPendingVerification(String phoneNumber) {
        String key = "verification_sms:" + phoneNumber;
        return redisTemplate.hasKey(key);
    }

    /**
     * Checks if phone number has pending MFA code
     */
    public boolean hasPendingMfaCode(String phoneNumber) {
        String key = "mfa_sms:" + phoneNumber;
        return redisTemplate.hasKey(key);
    }

    /**
     * Gets remaining time for code expiration
     */
    public long getRemainingCodeTime(String phoneNumber, String type) {
        String key = type + "_sms:" + phoneNumber;
        Long expiration = redisTemplate.getExpire(key);
        return expiration != null ? expiration : 0;
    }

    // Private helper methods

    private String generateSixDigitCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(0, 2) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }

    private String createMfaMessage(String code) {
        return String.format("Your MFA code is: %s. Valid for 5 minutes. Do not share this code.", code);
    }

    private String createVerificationMessage(String code) {
        return String.format("Your verification code is: %s. Valid for 5 minutes.", code);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    // SMS Provider implementations

    private void sendViaTwilio(String phoneNumber, String message) {
        // TODO: Implement Twilio SMS sending
        // You would use Twilio SDK here
        log.info("Sending SMS via Twilio to {}: {}", maskPhoneNumber(phoneNumber), message);
        
        // Example implementation (requires Twilio dependency):
        /*
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message.creator(
            new PhoneNumber(phoneNumber),
            new PhoneNumber(twilioFromNumber),
            message
        ).create();
        */
    }

    private void sendViaAWSSNS(String phoneNumber, String message) {
        // TODO: Implement AWS SNS SMS sending
        // You would use AWS SDK here
        log.info("Sending SMS via AWS SNS to {}: {}", maskPhoneNumber(phoneNumber), message);
        
        // Example implementation (requires AWS SDK):
        /*
        SnsClient snsClient = SnsClient.builder().build();
        PublishRequest request = PublishRequest.builder()
            .phoneNumber(phoneNumber)
            .message(message)
            .build();
        snsClient.publish(request);
        */
    }

    private void sendViaMock(String phoneNumber, String message) {
        // Mock implementation for development/testing
        log.info("MOCK SMS to {}: {}", maskPhoneNumber(phoneNumber), message);
        
        // Simulate network delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}