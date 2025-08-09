package com.mgaye.banking_application.service;

import com.mgaye.banking_application.dto.LoginAttemptDto;
import com.mgaye.banking_application.dto.TrustedDeviceDto;
import com.mgaye.banking_application.dto.UserRegistrationDto;
import com.mgaye.banking_application.dto.UserResponseDto;
import com.mgaye.banking_application.dto.UserSessionDto;
import com.mgaye.banking_application.dto.request.UpdateProfileRequest;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.entity.UserSession;
import com.mgaye.banking_application.entity.AlertSeverity;
import com.mgaye.banking_application.entity.AuditAction;
import com.mgaye.banking_application.entity.LoginAttempt;
import com.mgaye.banking_application.entity.Role;
import com.mgaye.banking_application.entity.SecurityAlertType;
import com.mgaye.banking_application.entity.TrustedDevice;
import com.mgaye.banking_application.exception.AccountAlreadyVerifiedException;
import com.mgaye.banking_application.exception.DeviceNotFoundException;
import com.mgaye.banking_application.exception.InvalidTokenException;
import com.mgaye.banking_application.exception.PasswordReusedException;
import com.mgaye.banking_application.exception.SessionNotFoundException;
import com.mgaye.banking_application.exception.TokenExpiredException;
import com.mgaye.banking_application.exception.UnauthorizedOperationException;
import com.mgaye.banking_application.exception.UserAlreadyExistsException;
import com.mgaye.banking_application.exception.UserNotFoundException;
import com.mgaye.banking_application.repository.LoginAttemptRepository;
import com.mgaye.banking_application.repository.TrustedDeviceRepository;
import com.mgaye.banking_application.repository.UserRepository;
import com.mgaye.banking_application.repository.UserSessionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// @Transactional
// public class UserService {

//     private final UserRepository userRepository;
//     private final PasswordEncoder passwordEncoder;
//     private final AuditService auditService;
//     private final TokenService tokenService;
//     private final TotpManager totpManager;
//     private final TotpService totpService;
//     private final NotificationService notificationService;
//     private final PasswordService passwordService;

//     public UserResponseDto registerUser(UserRegistrationDto registrationDto) {

//         String normalizedEmail = registrationDto.getEmail().trim().toLowerCase();

//         if (userRepository.existsByEmail(normalizedEmail)) {
//             throw new UserAlreadyExistsException("Email already exists");
//         }
//         // Create a new user entity
//         User user = new User();
//         user.setUsername(registrationDto.getUsername().trim());
//         user.setEmail(normalizedEmail);
//         user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
//         user.setFirstName(registrationDto.getFirstName().trim());
//         user.setLastName(registrationDto.getLastName().trim());
//         user.setPhoneNumber(registrationDto.getPhoneNumber().trim());
//         user.setAddress(registrationDto.getAddress().trim());
//         user.setDateOfBirth(java.time.LocalDate.parse(registrationDto.getDateOfBirth()));
//         user.setRole(Role.CUSTOMER);

//         // Account should be inactive and unverified at registration
//         user.setIsActive(false);
//         user.setIsVerified(false);
//         user.setAccountLockedUntil(null);

//         // Generate 2FA secret key (if you plan to support TOTP)
//         String mfaSecret = totpService.generateSecret();
//         user.setMfaSecret(mfaSecret);

//         // Optionally: generate email verification token or OTP
//         String verificationToken = tokenService.generateVerificationToken(user);
//         user.setVerificationToken(verificationToken);
//         user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

//         // Save user to DB
//         User savedUser = userRepository.save(user);

//         // Send verification email or SMS here (async preferred)
//         notificationService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

//         // Audit log
//         auditService.logAction("USER_REGISTERED", "User", savedUser.getId(),
//                 "New user registered: " + savedUser.getUsername());

//         return convertToResponseDto(savedUser);
//     }

//     @Transactional(readOnly = true)
//     public UserResponseDto getUserById(Long id) {
//         User user = userRepository.findById(id)
//                 .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
//         return convertToResponseDto(user);
//     }

//     @Transactional(readOnly = true)
//     public UserResponseDto getUserByUsername(String username) {
//         User user = userRepository.findByUsername(username)
//                 .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
//         return convertToResponseDto(user);
//     }

//     @Transactional(readOnly = true)
//     public UserResponseDto getUserByEmail(String email) {
//         User user = userRepository.findByEmail(email)
//                 .orElseThrow(() -> new UserNotFoundException("User not found with username: " + email));
//         return convertToResponseDto(user);
//     }

//     @Transactional(readOnly = true)
//     public List<UserResponseDto> getAllUsers() {
//         return userRepository.findAll().stream()
//                 .map(this::convertToResponseDto)
//                 .collect(Collectors.toList());
//     }

//     public void lockUser(Long userId, int hours) {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new UserNotFoundException("User not found"));

//         user.setAccountLockedUntil(LocalDateTime.now().plusHours(hours));
//         userRepository.save(user);

//         auditService.logAction("USER_LOCKED", "User", userId,
//                 "User locked for " + hours + " hours");
//     }

//     public void unlockUser(Long userId) {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new UserNotFoundException("User not found"));

//         user.setAccountLockedUntil(null);
//         user.setFailedLoginAttempts(0);
//         userRepository.save(user);

//         auditService.logAction("USER_UNLOCKED", "User", userId, "User unlocked");
//     }

//     public void incrementFailedLoginAttempts(String email) {
//         User user = userRepository.findByEmail(email)
//                 .orElseThrow(() -> new UserNotFoundException("User not found"));

//         user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

//         if (user.getFailedLoginAttempts() >= 5) {
//             user.setAccountLockedUntil(LocalDateTime.now().plusHours(24));
//             auditService.logAction("USER_AUTO_LOCKED", "User", user.getId(),
//                     "User auto-locked due to failed login attempts");
//         }

//         userRepository.save(user);
//     }

//     public void resetFailedLoginAttempts(String email) {
//         User user = userRepository.findByEmail(email)
//                 .orElseThrow(() -> new UserNotFoundException("User not found"));

//         user.setFailedLoginAttempts(0);
//         userRepository.save(user);
//     }

//     private UserResponseDto convertToResponseDto(User user) {
//         UserResponseDto dto = new UserResponseDto();
//         dto.setId(user.getId());
//         dto.setUsername(user.getUsername());
//         dto.setEmail(user.getEmail());
//         dto.setFirstName(user.getFirstName());
//         dto.setLastName(user.getLastName());
//         dto.setPhoneNumber(user.getPhoneNumber());
//         dto.setAddress(user.getAddress());
//         dto.setDateOfBirth(user.getDateOfBirth().toString());
//         dto.setRole(user.getRole().toString());
//         dto.setIsActive(user.getIsActive());
//         dto.setIsVerified(user.getIsVerified());
//         dto.setCreatedAt(user.getCreatedAt());
//         return dto;
//     }

//     // Add these methods to your UserService class

//     /**
//      * Verifies email using verification token
//      */
//     public void verifyEmail(String token, String ipAddress) {
//         if (token == null || token.trim().isEmpty()) {
//             throw new InvalidTokenException("Invalid verification token");
//         }

//         User user = userRepository.findByVerificationToken(token)
//                 .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

//         // Check if token is expired
//         if (user.getVerificationTokenExpiry() != null &&
//                 LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
//             throw new TokenExpiredException("Verification token has expired");
//         }

//         // Check if already verified
//         if (user.getIsVerified()) {
//             throw new AccountAlreadyVerifiedException("Email is already verified");
//         }

//         // Verify the user
//         user.setIsVerified(true);
//         user.setIsActive(true);
//         user.setVerificationToken(null);
//         user.setVerificationTokenExpiry(null);
//         user.setVerificationTokenExpiry(LocalDateTime.now());

//         userRepository.save(user);

//         // Send welcome email
//         notificationService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

//         // Log the action
//         auditService.logAction("EMAIL_VERIFIED", "User", user.getId(),
//                 "Email verified successfully from IP: " + ipAddress);

//         log.info("Email verified successfully for user: {}", user.getEmail());
//     }

//     /**
//      * Initiates password reset process
//      */
//     public void initiatePasswordReset(String email, String ipAddress) {
//         if (email == null || email.trim().isEmpty()) {
//             throw new IllegalArgumentException("Email cannot be empty");
//         }

//         String normalizedEmail = email.trim().toLowerCase();

//         // Always return success to prevent email enumeration attacks
//         // But only send email if user exists
//         Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

//         if (userOpt.isPresent()) {
//             User user = userOpt.get();

//             // Check if account is active
//             if (!user.getIsActive()) {
//                 log.warn("Password reset attempted for inactive account: {}", normalizedEmail);
//                 return; // Don't reveal that account is inactive
//             }

//             // Generate reset token
//             String resetToken = tokenService.generatePasswordResetToken(user);
//             user.setPasswordResetToken(resetToken);
//             user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

//             userRepository.save(user);

//             // Send password reset email
//             notificationService.sendPasswordResetEmail(user.getEmail(), resetToken);

//             // Log the action
//             auditService.logAction("PASSWORD_RESET_REQUESTED", "User", user.getId(),
//                     "Password reset requested from IP: " + ipAddress);

//             log.info("Password reset initiated for user: {}", normalizedEmail);
//         } else {
//             log.warn("Password reset requested for non-existent email: {}", normalizedEmail);
//             // Still log for security monitoring
//             auditService.logAction("PASSWORD_RESET_INVALID_EMAIL", "System", null,
//                     "Password reset requested for non-existent email: " + normalizedEmail + " from IP: " + ipAddress);
//         }
//     }

//     /**
//      * Resets password using reset token
//      */
//     public void resetPassword(String token, String newPassword, String ipAddress) {
//         if (token == null || token.trim().isEmpty()) {
//             throw new InvalidTokenException("Invalid reset token");
//         }

//         if (newPassword == null || newPassword.trim().isEmpty()) {
//             throw new IllegalArgumentException("New password cannot be empty");
//         }

//         User user = userRepository.findByPasswordResetToken(token)
//                 .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

//         // Check if token is expired
//         if (user.getPasswordResetTokenExpiry() != null &&
//                 LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
//             throw new TokenExpiredException("Reset token has expired");
//         }

//         // Validate new password strength
//         passwordService.validatePasswordStrength(newPassword);

//         // Check password history (optional - you might want to skip this for reset)
//         try {
//             passwordService.validatePasswordHistory(user, newPassword);
//         } catch (PasswordReusedException e) {
//             // For password reset, you might want to allow reuse or show a warning
//             log.warn("User attempting to reuse recent password during reset: {}", user.getEmail());
//         }

//         // Save current password to history before changing
//         if (user.getPassword() != null) {
//             passwordService.savePasswordHistory(user, user.getPassword(),
//                     "PASSWORD_RESET", ipAddress);
//         }

//         // Update password
//         user.setPassword(passwordEncoder.encode(newPassword));
//         user.setPasswordResetToken(null);
//         user.setPasswordResetTokenExpiry(null);
//         user.setPasswordChangedAt(LocalDateTime.now());

//         // Reset failed login attempts
//         user.setFailedLoginAttempts(0);
//         user.setAccountLockedUntil(null);

//         userRepository.save(user);

//         // Send confirmation email
//         notificationService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getFirstName());

//         // Log the action
//         auditService.logAction("PASSWORD_RESET_COMPLETED", "User", user.getId(),
//                 "Password reset completed from IP: " + ipAddress);

//         log.info("Password reset completed for user: {}", user.getEmail());
//     }

//     /**
//      * Changes password for authenticated user
//      */
//     public void changePassword(User user, String currentPassword, String newPassword, String ipAddress) {
//         if (currentPassword == null || currentPassword.trim().isEmpty()) {
//             throw new IllegalArgumentException("Current password cannot be empty");
//         }

//         if (newPassword == null || newPassword.trim().isEmpty()) {
//             throw new IllegalArgumentException("New password cannot be empty");
//         }

//         // Verify current password
//         if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
//             // Log failed attempt
//             auditService.logAction("PASSWORD_CHANGE_FAILED", "User", user.getId(),
//                     "Invalid current password provided from IP: " + ipAddress);
//             throw new IllegalArgumentException("Current password is incorrect");
//         }

//         // Validate new password strength
//         passwordService.validatePasswordStrength(newPassword);

//         // Check password history
//         passwordService.validatePasswordHistory(user, newPassword);

//         // Check if new password is same as current
//         if (passwordEncoder.matches(newPassword, user.getPassword())) {
//             throw new IllegalArgumentException("New password must be different from current password");
//         }

//         // Save current password to history
//         passwordService.savePasswordHistory(user, user.getPassword(),
//                 "PASSWORD_CHANGE", ipAddress);

//         // Update password
//         user.setPassword(passwordEncoder.encode(newPassword));
//         user.setPasswordChangedAt(LocalDateTime.now());

//         userRepository.save(user);

//         // Send confirmation email
//         notificationService.sendPasswordChangeConfirmationEmail(user.getEmail(), user.getFirstName());

//         // Log the action
//         auditService.logAction("PASSWORD_CHANGED", "User", user.getId(),
//                 "Password changed successfully from IP: " + ipAddress);

//         log.info("Password changed successfully for user: {}", user.getEmail());
//     }
// }

@Service
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final SecurityService securityService;
    private final UserSessionRepository sessionRepository;
    private final TrustedDeviceRepository trustedDeviceRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final TokenService tokenService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordService passwordService,
            EmailService emailService,
            AuditService auditService,
            SecurityService securityService,
            UserSessionRepository sessionRepository,
            TrustedDeviceRepository trustedDeviceRepository,
            LoginAttemptRepository loginAttemptRepository,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordService = passwordService;
        this.emailService = emailService;
        this.auditService = auditService;
        this.securityService = securityService;
        this.sessionRepository = sessionRepository;
        this.trustedDeviceRepository = trustedDeviceRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.tokenService = tokenService;
    }

    // public UserResponseDto getUserById(Long id){
    // User user = userRepository.getById(id)
    // .orElseThrow(() -> new UserNotFoundException("User not found with username: "
    // + user));

    // return UserResponseDto.fromUser(user);
    // }

    // public List<UserResponseDto> getAllUsers(Long id) {
    // List<User> user = userRepository.findAll()
    // .orElseThrow(() -> UserNotFoundException("User not found with username: " +
    // user));

    // return UserResponseDto.fromUser(user);
    // }

    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return UserResponseDto.fromUser(user);
    }

    public void verifyEmail(String token, String ipAddress) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Verification token has expired");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        auditService.log(AuditAction.EMAIL_VERIFIED, user, ipAddress, "Email verified successfully");
        log.info("Email verified for user: {}", user.getEmail());
    }

    public void initiatePasswordReset(String email, String ipAddress) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!user.getIsActive() || user.getIsSuspended()) {
                // Don't reveal account status for security
                return;
            }

            String resetToken = tokenService.generatePasswordResetToken(user);
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user, resetToken);
            auditService.log(AuditAction.PASSWORD_RESET_INITIATED, user, ipAddress,
                    "Password reset initiated");
        }

        // Always return success to prevent email enumeration
        log.info("Password reset initiated for email: {}", email);
    }

    public void resetPassword(String token, String newPassword, String ipAddress) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid reset token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Reset token has expired");
        }

        // Validate password strength
        passwordService.validatePasswordStrength(newPassword);

        // Check password history
        passwordService.validatePasswordHistory(user, newPassword);

        // Save old password to history
        passwordService.savePasswordHistory(user, user.getPassword(), "PASSWORD_RESET", ipAddress);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setForcePasswordChange(false);

        userRepository.save(user);

        // Invalidate all sessions for security
        invalidateAllUserSessions(user);

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(user);

        auditService.log(AuditAction.PASSWORD_CHANGE, user, ipAddress, "Password reset successfully");
        securityService.createAlert(user, SecurityAlertType.PASSWORD_CHANGED,
                AlertSeverity.MEDIUM, "Password was reset", ipAddress);

        log.info("Password reset completed for user: {}", user.getEmail());
    }

    public void changePassword(User user, String currentPassword, String newPassword, String ipAddress) {
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Validate new password strength
        passwordService.validatePasswordStrength(newPassword);

        // Check password history
        passwordService.validatePasswordHistory(user, newPassword);

        // Save old password to history
        passwordService.savePasswordHistory(user, user.getPassword(), "PASSWORD_CHANGE", ipAddress);

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
        user.setForcePasswordChange(false);

        userRepository.save(user);

        // Send confirmation email
        emailService.sendPasswordChangeConfirmationEmail(user);

        auditService.log(AuditAction.PASSWORD_CHANGE, user, ipAddress, "Password changed by user");
        securityService.createAlert(user, SecurityAlertType.PASSWORD_CHANGED,
                AlertSeverity.LOW, "Password changed by user", ipAddress);

        log.info("Password changed for user: {}", user.getEmail());
    }

    public User updateProfile(User user, UpdateProfileRequest request, String ipAddress) {
        String oldValues = buildOldValuesJson(user);
        boolean changed = false;

        if (request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
            changed = true;
        }

        if (request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
            changed = true;
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
            changed = true;
        }

        if (request.getAddress() != null && !request.getAddress().equals(user.getAddress())) {
            user.setAddress(request.getAddress());
            changed = true;
        }

        if (request.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
            changed = true;
        }

        if (request.getSmsNotificationsEnabled() != null) {
            user.setSmsNotificationsEnabled(request.getSmsNotificationsEnabled());
            changed = true;
        }

        if (changed) {
            user.setUpdatedBy(user.getEmail());
            User savedUser = userRepository.save(user);

            String newValues = buildNewValuesJson(savedUser);
            auditService.logWithDetails(AuditAction.PROFILE_UPDATE, user, ipAddress,
                    "Profile updated", oldValues, newValues);

            log.info("Profile updated for user: {}", user.getEmail());
            return savedUser;
        }

        return user;
    }

    public List<UserSessionDto> getActiveSessions(User user) {
        List<UserSession> sessions = sessionRepository
                .findByUserAndIsActiveTrueOrderByLastAccessedAtDesc(user);

        return sessions.stream()
                .map(this::convertToSessionDto)
                .collect(Collectors.toList());
    }

    public void terminateSession(User user, String sessionId, String ipAddress) {
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Cannot terminate session of another user");
        }

        session.setIsActive(false);
        sessionRepository.save(session);

        auditService.log(AuditAction.SESSION_TERMINATED, user, ipAddress,
                "Session terminated by user: " + sessionId);

        log.info("Session {} terminated for user: {}", sessionId, user.getEmail());
    }

    public List<TrustedDeviceDto> getTrustedDevices(User user) {
        List<TrustedDevice> devices = trustedDeviceRepository
                .findByUserAndIsActiveTrueOrderByLastUsedAtDesc(user);

        return devices.stream()
                .map(this::convertToDeviceDto)
                .collect(Collectors.toList());
    }

    public void removeTrustedDevice(User user, Long deviceId, String ipAddress) {
        TrustedDevice device = trustedDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Trusted device not found"));

        if (!device.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Cannot remove device of another user");
        }

        device.setIsActive(false);
        trustedDeviceRepository.save(device);

        auditService.log(AuditAction.DEVICE_REMOVED, user, ipAddress,
                "Trusted device removed: " + device.getDeviceName());

        log.info("Trusted device {} removed for user: {}", device.getDeviceName(), user.getEmail());
    }

    public List<LoginAttemptDto> getLoginHistory(User user, int page, int size) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<LoginAttempt> attempts = loginAttemptRepository
                .findByUserAndAttemptTimeAfterOrderByAttemptTimeDesc(user, thirtyDaysAgo);

        return attempts.stream()
                .skip(page * size)
                .limit(size)
                .map(this::convertToLoginAttemptDto)
                .collect(Collectors.toList());
    }

    private void invalidateAllUserSessions(User user) {
        List<UserSession> sessions = sessionRepository
                .findByUserAndIsActiveTrueOrderByLastAccessedAtDesc(user);
        sessions.forEach(session -> session.setIsActive(false));
        sessionRepository.saveAll(sessions);
    }

    private String buildOldValuesJson(User user) {
        return "{\"firstName\":\"" + user.getFirstName() +
                "\",\"lastName\":\"" + user.getLastName() +
                "\",\"phoneNumber\":\"" + user.getPhoneNumber() +
                "\",\"address\":\"" + user.getAddress() + "\"}";
    }

    private String buildNewValuesJson(User user) {
        return "{\"firstName\":\"" + user.getFirstName() +
                "\",\"lastName\":\"" + user.getLastName() +
                "\",\"phoneNumber\":\"" + user.getPhoneNumber() +
                "\",\"address\":\"" + user.getAddress() + "\"}";
    }

    private UserSessionDto convertToSessionDto(UserSession session) {
        return UserSessionDto.builder()
                .sessionId(session.getSessionId())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .createdAt(session.getCreatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .expiresAt(session.getExpiresAt())
                .isActive(session.getIsActive())
                .build();
    }

    private TrustedDeviceDto convertToDeviceDto(TrustedDevice device) {
        return TrustedDeviceDto.builder()
                .id(device.getId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType())
                .browser(device.getBrowser())
                .operatingSystem(device.getOperatingSystem())
                .location(device.getLocation())
                .trustedAt(device.getTrustedAt())
                .lastUsedAt(device.getLastUsedAt())
                .build();
    }

    private LoginAttemptDto convertToLoginAttemptDto(LoginAttempt attempt) {
        return LoginAttemptDto.builder()
                .id(attempt.getId())
                .ipAddress(attempt.getIpAddress())
                .userAgent(attempt.getUserAgent())
                .attemptTime(attempt.getAttemptTime())
                .success(attempt.getSuccess())
                .failureReason(attempt.getFailureReason())
                .location(attempt.getLocation())
                .blocked(attempt.getBlocked())
                .build();
    }
}
