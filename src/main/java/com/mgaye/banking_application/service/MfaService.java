package com.mgaye.banking_application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mgaye.banking_application.dto.response.MfaSetupResponse;
import com.mgaye.banking_application.entity.MfaMethod;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.InvalidCredentialsException;
import com.mgaye.banking_application.exception.InvalidMfaCodeException;
import com.mgaye.banking_application.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MfaService {

    private final TotpManager totpManager;
    private final SmsService smsService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // public MfaService(TotpManager totpManager, SmsService smsService,
    // EmailService emailService, PasswordEncoder passwordEncoder) {
    // this.totpManager = totpManager;
    // this.smsService = smsService;
    // this.emailService = emailService;
    // this.passwordEncoder = passwordEncoder;
    // }

    public String generateMfaSecret() {
        return totpManager.generateSecret();
    }

    public String generateQrCodeUrl(User user, String secret) {
        return totpManager.getQrCodeUrl(user.getEmail(), secret);
    }

    public boolean verifyMfaCode(User user, String code) {
        switch (user.getMfaMethod()) {
            case TOTP:
                return totpManager.verifyCode(user.getMfaSecret(), code);
            case SMS:
                return smsService.verifyCode(user.getPhoneNumber(), code);
            case EMAIL:
                return emailService.verifyCode(user.getEmail(), code);
            default:
                return false;
        }
    }

    public void sendMfaCode(User user) {
        switch (user.getMfaMethod()) {
            case SMS:
                smsService.sendMfaCode(user.getPhoneNumber());
                break;
            case EMAIL:
                emailService.verifyCode(user.getEmail(), user.getMfaSecret());
                break;
        }
    }

    public List<String> generateBackupCodes() {
        return IntStream.range(0, 10)
                .mapToObj(i -> generateBackupCode())
                .collect(Collectors.toList());
    }

    private String generateBackupCode() {
        return RandomStringUtils.randomAlphanumeric(8).toUpperCase();
    }

    // Add these methods to your MfaService class

    private final UserRepository userRepository; // Add this dependency
    private final AuditService auditService; // Add this dependency

    /**
     * Sets up MFA for a user
     */
    public MfaSetupResponse setupMfa(User user, MfaMethod method) {
        if (user.getIsMfaEnabled()) {
            throw new IllegalStateException("MFA is already enabled for this user");
        }

        switch (method) {
            case TOTP:
                return setupTotpMfa(user);
            case SMS:
                return setupSmsMfa(user);
            case EMAIL:
                return setupEmailMfa(user);
            default:
                throw new IllegalArgumentException("Unsupported MFA method: " + method);
        }
    }

    /**
     * Verifies MFA setup and enables MFA for the user
     */
    public void verifyAndEnableMfa(User user, String code, List<String> backupCodes, String ipAddress) {
        if (user.getIsMfaEnabled()) {
            throw new IllegalStateException("MFA is already enabled for this user");
        }

        // Verify the provided code
        boolean isValid = verifyMfaCode(user, code);
        if (!isValid) {
            auditService.logAction("MFA_SETUP_FAILED", "User", user.getId(),
                    "Invalid MFA code during setup from IP: " + ipAddress);
            throw new InvalidMfaCodeException("Invalid MFA code");
        }

        // Enable MFA
        user.setIsMfaEnabled(true);
        user.setMfaEnabledAt(LocalDateTime.now());

        // Store backup codes if provided
        if (backupCodes != null && !backupCodes.isEmpty()) {
            storeBackupCodes(user, backupCodes);
        }

        userRepository.save(user);

        // Log successful MFA setup
        auditService.logAction("MFA_ENABLED", "User", user.getId(),
                "MFA enabled successfully from IP: " + ipAddress);

        log.info("MFA enabled successfully for user: {}", user.getEmail());
    }

    /**
     * Disables MFA for a user
     */
    public void disableMfa(User user, String password, String ipAddress) {
        if (!user.getIsMfaEnabled()) {
            throw new IllegalStateException("MFA is not enabled for this user");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            auditService.logAction("MFA_DISABLE_FAILED", "User", user.getId(),
                    "Invalid password during MFA disable from IP: " + ipAddress);
            throw new InvalidCredentialsException("Invalid password");
        }

        // Disable MFA
        user.setIsMfaEnabled(false);
        user.setMfaMethod(null);
        user.setMfaSecret(null);
        user.setMfaEnabledAt(null);

        // Clear backup codes
        clearBackupCodes(user);

        userRepository.save(user);

        // Log MFA disable
        auditService.logAction("MFA_DISABLED", "User", user.getId(),
                "MFA disabled from IP: " + ipAddress);

        log.info("MFA disabled for user: {}", user.getEmail());
    }

    /**
     * Generates new backup codes for a user
     */
    public List<String> generateNewBackupCodes(User user) {
        if (!user.getIsMfaEnabled()) {
            throw new IllegalStateException("MFA must be enabled to generate backup codes");
        }

        List<String> newCodes = generateBackupCodes();
        storeBackupCodes(user, newCodes);

        auditService.logAction("BACKUP_CODES_GENERATED", "User", user.getId(),
                "New backup codes generated");

        log.info("New backup codes generated for user: {}", user.getEmail());
        return newCodes;
    }

    // Private helper methods

    private MfaSetupResponse setupTotpMfa(User user) {
        String secret = totpManager.generateSecret();
        String qrCodeUrl = totpManager.getQrCodeUrl(user.getEmail(), secret);

        // Store temporary secret (not yet enabled)
        user.setMfaMethod(MfaMethod.TOTP);
        user.setMfaSecret(secret);
        userRepository.save(user);

        return MfaSetupResponse.builder()
                .method(MfaMethod.TOTP)
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .message("Scan the QR code with Google Authenticator and enter the verification code")
                .requiresVerification(true)
                .expirationMinutes(10)
                .build();
    }

    private MfaSetupResponse setupSmsMfa(User user) {
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalStateException("Phone number is required for SMS MFA");
        }

        user.setMfaMethod(MfaMethod.SMS);
        userRepository.save(user);

        // Send verification SMS
        smsService.sendMfaCode(user.getPhoneNumber());

        return MfaSetupResponse.builder()
                .method(MfaMethod.SMS)
                .message("Verification code sent to your phone number")
                .requiresVerification(true)
                .expirationMinutes(5)
                .maskedContact(maskPhoneNumber(user.getPhoneNumber()))
                .build();
    }

    private MfaSetupResponse setupEmailMfa(User user) {
        user.setMfaMethod(MfaMethod.EMAIL);
        userRepository.save(user);

        // Send verification email
        emailService.sendMfaCode(user.getEmail());

        return MfaSetupResponse.builder()
                .method(MfaMethod.EMAIL)
                .message("Verification code sent to your email address")
                .requiresVerification(true)
                .expirationMinutes(10)
                .maskedContact(maskEmail(user.getEmail()))
                .build();
    }

    private void storeBackupCodes(User user, List<String> codes) {
        // You might want to create a separate entity for backup codes
        // For now, we'll store them as JSON in the user entity
        // In production, store hashed versions of the codes
        String encodedCodes = String.join(",", codes);
        user.setBackupCodes(encodedCodes); // Add this field to User entity
        userRepository.save(user);
    }

    private void clearBackupCodes(User user) {
        user.setBackupCodes(null);
        userRepository.save(user);
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(0, 2) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****@****.com";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return "**@" + domain;
        }
        return local.substring(0, 2) + "****@" + domain;
    }
}