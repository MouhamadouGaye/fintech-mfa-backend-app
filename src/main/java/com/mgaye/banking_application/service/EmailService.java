package com.mgaye.banking_application.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mgaye.banking_application.dto.DeviceInfo;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.EmailServiceException;

@Slf4j
@Service
public class EmailService {

    private final RedisTemplate<String, String> redisTemplate; // Add this field
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(RedisTemplate<String, String> redisTemplate, JavaMailSender mailSender,
            TemplateEngine templateEngine) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;

    }

    @Value("${app.frontend.url}")
    private String frontendUrl; // for verification link

    @Value("${app.mail.from}")
    private String fromEmail; // for the From: header

    public void sendVerificationEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("verificationUrl",
                    frontendUrl + "/verify-email?token=" + user.getVerificationToken());

            String htmlContent = templateEngine.process("email/email-verification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your Banking Account");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new EmailServiceException("Failed to send verification email");
        }

    }

    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("resetUrl",
                    frontendUrl + "/reset-password?token=" + resetToken);

            String htmlContent = templateEngine.process("email/password-reset", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Reset Your Password");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new EmailServiceException("Failed to send password reset email");
        }
    }

    public void sendPasswordResetConfirmationEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);

            String htmlContent = templateEngine.process("email/password-reset-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Successful");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email", e);
        }
    }

    public void sendPasswordChangeConfirmationEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);

            String htmlContent = templateEngine.process("email/password-change-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Password Changed Successfully");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send password change confirmation email", e);
        }
    }

    public void sendAccountLockedEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("lockoutTime", user.getAccountLockedUntil());

            String htmlContent = templateEngine.process("email/account-locked", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Account Temporarily Locked");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send account locked email", e);
        }
    }

    public void sendUnusualLocationAlert(User user, String ipAddress) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("loginTime", LocalDateTime.now());

            String htmlContent = templateEngine.process("email/unusual-location", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Unusual Login Location Detected");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send unusual location alert", e);
        }
    }

    public void sendDeviceVerificationEmail(User user, String code, DeviceInfo deviceInfo) {
        try {
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("verificationCode", code);
            context.setVariable("deviceInfo", deviceInfo);

            String htmlContent = templateEngine.process("email/device-verification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("New Device Verification Required");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send device verification email", e);
            throw new EmailServiceException("Failed to send device verification email");
        }
    }

    /**
     * Sends MFA code via email
     */
    public void sendMfaCode(String email) {
        try {
            String code = generateSixDigitCode();
            String key = "mfa_email:" + email;

            // Store code in Redis with 5 minute expiration
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));

            // Find user by email (you might need to inject UserRepository)
            // For now, creating a context with just email and code
            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("mfaCode", code);
            context.setVariable("expirationMinutes", 5);

            String htmlContent = templateEngine.process("email/mfa-code", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your MFA Code");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("MFA code sent to email: {}", maskEmail(email));

        } catch (Exception e) {
            log.error("Failed to send MFA code to email: {}", email, e);
            throw new EmailServiceException("Failed to send MFA code");
        }
    }

    /**
     * Verifies MFA code sent via email
     */
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null || code.trim().isEmpty()) {
            return false;
        }

        String key = "mfa_email:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && constantTimeEquals(code.trim(), storedCode)) {
            // Delete the code after successful verification
            redisTemplate.delete(key);
            log.info("Email MFA code verified successfully for: {}", maskEmail(email));
            return true;
        }

        log.warn("Invalid email MFA code attempt for: {}", maskEmail(email));
        return false;
    }

    /**
     * Sends email verification code (different from account verification)
     */
    public void sendEmailVerificationCode(String email) {
        try {
            String code = generateSixDigitCode();
            String key = "email_verification:" + email;

            // Store code in Redis with 10 minute expiration
            redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(10));

            Context context = new Context();
            context.setVariable("email", email);
            context.setVariable("verificationCode", code);
            context.setVariable("expirationMinutes", 10);

            String htmlContent = templateEngine.process("email/email-verification-code", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Email Verification Code");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email verification code sent to: {}", maskEmail(email));

        } catch (Exception e) {
            log.error("Failed to send email verification code to: {}", email, e);
            throw new EmailServiceException("Failed to send email verification code");
        }
    }

    /**
     * Verifies email verification code
     */
    public boolean verifyEmailVerificationCode(String email, String code) {
        if (email == null || code == null || code.trim().isEmpty()) {
            return false;
        }

        String key = "email_verification:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode != null && constantTimeEquals(code.trim(), storedCode)) {
            redisTemplate.delete(key);
            log.info("Email verification code verified successfully for: {}", maskEmail(email));
            return true;
        }

        log.warn("Invalid email verification code attempt for: {}", maskEmail(email));
        return false;
    }

    // Helper methods
    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1000000));
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return "**@" + domain;
        }
        return local.substring(0, 2) + "****@" + domain;
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
}
