package com.mgaye.banking_application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class NotificationService {

//     private final JavaMailSender mailSender;

//     public NotificationService(JavaMailSender mailSender) {
//         this.mailSender = mailSender;
//     }

//     public void sendVerificationEmail(String email, String verificationToken) {
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             message.setTo(email);
//             message.setSubject("Email Verification Required");
//             message.setText(createVerificationEmailBody(verificationToken));
//             message.setFrom("noreply@yourapp.com");

//             mailSender.send(message);
//             log.info("Verification email sent to: {}", email);
//         } catch (Exception e) {
//             log.error("Failed to send verification email to: {}", email, e);
//             throw new RuntimeException("Failed to send verification email", e);
//         }
//     }

//     public void sendPasswordResetEmail(String email, String resetToken) {
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             message.setTo(email);
//             message.setSubject("Password Reset Request");
//             message.setText(createPasswordResetEmailBody(resetToken));
//             message.setFrom("noreply@yourapp.com");

//             mailSender.send(message);
//             log.info("Password reset email sent to: {}", email);
//         } catch (Exception e) {
//             log.error("Failed to send password reset email to: {}", email, e);
//             throw new RuntimeException("Failed to send password reset email", e);
//         }
//     }

//     public void sendWelcomeEmail(String email, String username) {
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             message.setTo(email);
//             message.setSubject("Welcome to Our Platform!");
//             message.setText(createWelcomeEmailBody(username));
//             message.setFrom("noreply@yourapp.com");

//             mailSender.send(message);
//             log.info("Welcome email sent to: {}", email);
//         } catch (Exception e) {
//             log.error("Failed to send welcome email to: {}", email, e);
//             throw new RuntimeException("Failed to send welcome email", e);
//         }
//     }

//     private String createVerificationEmailBody(String verificationToken) {
//         return String.format(
//                 "Welcome to our platform!\n\n" +
//                         "Please click the following link to verify your email address:\n" +
//                         "http://localhost:8080/api/auth/verify?token=%s\n\n" +
//                         "This link will expire in 24 hours.\n\n" +
//                         "If you didn't create an account, please ignore this email.\n\n" +
//                         "Best regards,\n" +
//                         "Your App Team",
//                 verificationToken);
//     }

//     private String createPasswordResetEmailBody(String resetToken) {
//         return String.format(
//                 "You have requested to reset your password.\n\n" +
//                         "Please click the following link to reset your password:\n" +
//                         "http://localhost:8080/api/auth/reset-password?token=%s\n\n" +
//                         "This link will expire in 1 hour.\n\n" +
//                         "If you didn't request a password reset, please ignore this email.\n\n" +
//                         "Best regards,\n" +
//                         "Your App Team",
//                 resetToken);
//     }

//     private String createWelcomeEmailBody(String username) {
//         return String.format(
//                 "Dear %s,\n\n" +
//                         "Welcome to our platform! Your account has been successfully verified.\n\n" +
//                         "You can now log in and start using our services.\n\n" +
//                         "If you have any questions, feel free to contact our support team.\n\n" +
//                         "Best regards,\n" +
//                         "Your App Team",
//                 username);
//     }

//     public void sendSecurityAlert(Object user, Object alert) {
//         try {
//             SimpleMailMessage message = new SimpleMailMessage();
//             // Assuming user has getEmail() method and alert has getMessage() method
//             String userEmail = getUserEmail(user);
//             String alertMessage = getAlertMessage(alert);

//             message.setTo(userEmail);
//             message.setSubject("Security Alert - Immediate Attention Required");
//             message.setText(createSecurityAlertBody(alertMessage));
//             message.setFrom("security@yourapp.com");

//             mailSender.send(message);
//             log.info("Security alert sent to: {}", userEmail);
//         } catch (Exception e) {
//             log.error("Failed to send security alert", e);
//             throw new RuntimeException("Failed to send security alert", e);
//         }
//     }

//     private String createSecurityAlertBody(String alertMessage) {
//         return String.format(
//                 "SECURITY ALERT\n\n" +
//                         "%s\n\n" +
//                         "If this was not you, please:\n" +
//                         "1. Change your password immediately\n" +
//                         "2. Review your account activity\n" +
//                         "3. Contact our support team\n\n" +
//                         "Security Team\n" +
//                         "Your App",
//                 alertMessage);
//     }

//     private String getUserEmail(Object user) {
//         // This is a simple implementation - adjust based on your User class
//         try {
//             return (String) user.getClass().getMethod("getEmail").invoke(user);
//         } catch (Exception e) {
//             log.error("Could not extract email from user object", e);
//             return "unknown@example.com";
//         }
//     }

//     private String getAlertMessage(Object alert) {
//         // This is a simple implementation - adjust based on your SecurityAlert class
//         try {
//             return (String) alert.getClass().getMethod("getMessage").invoke(alert);
//         } catch (Exception e) {
//             log.error("Could not extract message from alert object", e);
//             return "Security alert detected";
//         }
//     }
// }

@Slf4j
@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String email, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Email Verification Required");
            message.setText(createVerificationEmailBody(verificationToken));
            message.setFrom("noreply@yourapp.com");

            mailSender.send(message);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request");
            message.setText(createPasswordResetEmailBody(resetToken));
            message.setFrom("noreply@yourapp.com");

            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendWelcomeEmail(String email, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Welcome to Our Platform!");
            message.setText(createWelcomeEmailBody(username));
            message.setFrom("noreply@yourapp.com");

            mailSender.send(message);
            log.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    private String createVerificationEmailBody(String verificationToken) {
        return String.format(
                "Welcome to our platform!\n\n" +
                        "Please click the following link to verify your email address:\n" +
                        "http://localhost:8080/api/auth/verify?token=%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Your App Team",
                verificationToken);
    }

    private String createPasswordResetEmailBody(String resetToken) {
        return String.format(
                "You have requested to reset your password.\n\n" +
                        "Please click the following link to reset your password:\n" +
                        "http://localhost:8080/api/auth/reset-password?token=%s\n\n" +
                        "This link will expire in 1 hour.\n\n" +
                        "If you didn't request a password reset, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Your App Team",
                resetToken);
    }

    private String createWelcomeEmailBody(String username) {
        return String.format(
                "Dear %s,\n\n" +
                        "Welcome to our platform! Your account has been successfully verified.\n\n" +
                        "You can now log in and start using our services.\n\n" +
                        "If you have any questions, feel free to contact our support team.\n\n" +
                        "Best regards,\n" +
                        "Your App Team",
                username);
    }

    public void sendSecurityAlert(Object user, Object alert) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Assuming user has getEmail() method and alert has getMessage() method
            String userEmail = getUserEmail(user);
            String alertMessage = getAlertMessage(alert);

            message.setTo(userEmail);
            message.setSubject("Security Alert - Immediate Attention Required");
            message.setText(createSecurityAlertBody(alertMessage));
            message.setFrom("security@yourapp.com");

            mailSender.send(message);
            log.info("Security alert sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send security alert", e);
            throw new RuntimeException("Failed to send security alert", e);
        }
    }

    private String createSecurityAlertBody(String alertMessage) {
        return String.format(
                "SECURITY ALERT\n\n" +
                        "%s\n\n" +
                        "If this was not you, please:\n" +
                        "1. Change your password immediately\n" +
                        "2. Review your account activity\n" +
                        "3. Contact our support team\n\n" +
                        "Security Team\n" +
                        "Your App",
                alertMessage);
    }

    private String getUserEmail(Object user) {
        // This is a simple implementation - adjust based on your User class
        try {
            return (String) user.getClass().getMethod("getEmail").invoke(user);
        } catch (Exception e) {
            log.error("Could not extract email from user object", e);
            return "unknown@example.com";
        }
    }

    private String getAlertMessage(Object alert) {
        // This is a simple implementation - adjust based on your SecurityAlert class
        try {
            return (String) alert.getClass().getMethod("getMessage").invoke(alert);
        } catch (Exception e) {
            log.error("Could not extract message from alert object", e);
            return "Security alert detected";
        }
    }

    public void sendPasswordResetConfirmationEmail(String email, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Successful");
            message.setText(createPasswordResetConfirmationBody(firstName));
            message.setFrom("noreply@yourapp.com");

            mailSender.send(message);
            log.info("Password reset confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset confirmation email", e);
        }
    }

    public void sendPasswordChangeConfirmationEmail(String email, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Changed Successfully");
            message.setText(createPasswordChangeConfirmationBody(firstName));
            message.setFrom("noreply@yourapp.com");

            mailSender.send(message);
            log.info("Password change confirmation email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password change confirmation email to: {}", email, e);
            throw new RuntimeException("Failed to send password change confirmation email", e);
        }
    }

    private String createPasswordResetConfirmationBody(String firstName) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your password has been successfully reset.\n\n" +
                        "If you did not initiate this password reset, please contact our support team immediately.\n\n"
                        +
                        "For your security:\n" +
                        "- Never share your password with anyone\n" +
                        "- Use a strong, unique password\n" +
                        "- Enable two-factor authentication if available\n\n" +
                        "Best regards,\n" +
                        "Your App Security Team",
                firstName != null ? firstName : "User");
    }

    private String createPasswordChangeConfirmationBody(String firstName) {
        return String.format(
                "Dear %s,\n\n" +
                        "Your password has been successfully changed.\n\n" +
                        "If you did not make this change, please contact our support team immediately and consider:\n" +
                        "- Changing your password again\n" +
                        "- Reviewing your account activity\n" +
                        "- Enabling additional security measures\n\n" +
                        "Thank you for keeping your account secure.\n\n" +
                        "Best regards,\n" +
                        "Your App Security Team",
                firstName != null ? firstName : "User");
    }
}