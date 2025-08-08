
package com.mgaye.banking_application.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mgaye.banking_application.dto.DeviceInfo;
import com.mgaye.banking_application.dto.UserDto;
import com.mgaye.banking_application.dto.request.AuthenticationRequest;
import com.mgaye.banking_application.dto.request.RegistrationRequest;
import com.mgaye.banking_application.dto.response.AuthenticationResponse;
import com.mgaye.banking_application.dto.response.RegistrationResponse;
import com.mgaye.banking_application.entity.AlertSeverity;
import com.mgaye.banking_application.entity.AmlStatus;
import com.mgaye.banking_application.entity.AuditAction;
import com.mgaye.banking_application.entity.SecurityAlertType;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.entity.UserSession;
import com.mgaye.banking_application.exception.AccountFrozenException;
import com.mgaye.banking_application.exception.AccountLockedException;
import com.mgaye.banking_application.exception.AccountNotVerifiedException;
import com.mgaye.banking_application.exception.AccountSuspendedException;
import com.mgaye.banking_application.exception.ComplianceException;
import com.mgaye.banking_application.exception.InvalidTokenException;
import com.mgaye.banking_application.exception.PasswordChangeRequiredException;
import com.mgaye.banking_application.exception.TooManyAttemptsException;
import com.mgaye.banking_application.exception.UserAlreadyExistsException;
import com.mgaye.banking_application.repository.UserRepository;
import com.mgaye.banking_application.security.JwtTokenProvider;
import com.mgaye.banking_application.utility.ClientIpAddress;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final JwtTokenProvider jwtTokenProvider;

    private final EmailService emailService;
    private final SmsService smsService;
    private final SecurityService securityService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;
    private final DeviceService deviceService;
    private final MfaService mfaService;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;
    private final ClientIpAddress clientIpAddress;

    public AuthenticationService(UserRepository userRepository,
            PasswordEncoder passwordEncoder, PasswordService passwordService,
            JwtTokenProvider jwtTokenProvider, EmailService emailService, SmsService smsService,
            SecurityService securityService, AuditService auditService, RateLimitService rateLimitService,
            DeviceService deviceService, MfaService mfaService, TokenService tokenService,
            LoginAttemptService loginAttenptService, ClientIpAddress clientIpAddress) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordService = passwordService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
        this.smsService = smsService;
        this.securityService = securityService;
        this.auditService = auditService;
        this.rateLimitService = rateLimitService;
        this.deviceService = deviceService;
        this.mfaService = mfaService;
        this.tokenService = tokenService;
        this.loginAttemptService = loginAttemptService;
        this.clientIpAddress = clientIpAddress;
    }

    // ✅ Email-based Authentication
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        String ipAddress = clientIpAddress.getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Rate limiting
        if (rateLimitService.isBlocked(ipAddress)) {
            throw new TooManyAttemptsException("Too many attempts from this IP address");
        }

        try {
            // Find user by email
            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            // Log login attempt
            loginAttemptService.recordAttempt(email, ipAddress, userAgent, false, "Authentication started");

            // Comprehensive account validation
            validateAccountForLogin(user, ipAddress, userAgent);

            // Password verification
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                handleFailedLogin(user, ipAddress, "Invalid password");
                throw new BadCredentialsException("Invalid email or password");
            }

            // Device verification
            DeviceInfo deviceInfo = deviceService.extractDeviceInfo(httpRequest);
            boolean isKnownDevice = deviceService.isKnownDevice(user, deviceInfo);
            boolean requiresDeviceVerification = !isKnownDevice && user.getIsMfaEnabled();

            // MFA verification (if enabled)
            if (user.getIsMfaEnabled()) {
                if (request.getMfaCode() == null) {
                    return AuthenticationResponse.builder()
                            .requiresMfa(true)
                            .requiresDeviceVerification(requiresDeviceVerification)
                            .message("MFA code required")
                            .build();
                }

                if (!mfaService.verifyMfaCode(user, request.getMfaCode())) {
                    handleFailedLogin(user, ipAddress, "Invalid MFA code");
                    throw new BadCredentialsException("Invalid MFA code");
                }
            }

            // Device verification (if required)
            if (requiresDeviceVerification) {
                if (request.getDeviceVerificationCode() == null) {
                    deviceService.sendDeviceVerificationCode(user, deviceInfo);
                    return AuthenticationResponse.builder()
                            .requiresDeviceVerification(true)
                            .message("Device verification required")
                            .build();
                }

                if (!deviceService.verifyDeviceCode(user, request.getDeviceVerificationCode())) {
                    handleFailedLogin(user, ipAddress, "Invalid device verification code");
                    throw new BadCredentialsException("Invalid device verification code");
                }

                // Add device to trusted devices
                deviceService.trustDevice(user, deviceInfo);
            }

            // Successful authentication
            handleSuccessfulLogin(user, ipAddress, userAgent);

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(user);
            String refreshToken = jwtTokenProvider.generateRefreshToken(user);

            // Create user session
            UserSession session = sessionService.createSession(user, httpRequest, accessToken);

            // Audit log
            auditService.log(AuditAction.USER_LOGIN, user, ipAddress, userAgent);

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.from(user))
                    .expiresIn(jwtTokenProvider.getAccessTokenValidityInSeconds())
                    .message("Authentication successful")
                    .build();

        } catch (Exception e) {
            rateLimitService.recordFailedAttempt(ipAddress);
            loginAttemptService.recordAttempt(email, ipAddress, userAgent, false, e.getMessage());
            throw e;
        }
    }

    private void validateAccountForLogin(User user, String ipAddress, String userAgent) {
        if (!user.getIsActive()) {
            securityService.createAlert(user, SecurityAlertType.SUSPICIOUS_LOGIN,
                    AlertSeverity.HIGH, "Login attempt on inactive account", ipAddress);
            throw new DisabledException("Account is not active");
        }

        if (!user.getIsVerified()) {
            throw new AccountNotVerifiedException("Account email not verified");
        }

        if (user.getIsSuspended()) {
            securityService.createAlert(user, SecurityAlertType.SUSPICIOUS_LOGIN,
                    AlertSeverity.HIGH, "Login attempt on suspended account", ipAddress);
            throw new AccountSuspendedException("Account is suspended: " + user.getSuspensionReason());
        }

        if (user.getIsFrozen()) {
            throw new AccountFrozenException("Account is frozen: " + user.getFreezeReason());
        }

        if (user.isAccountLocked()) {
            throw new AccountLockedException("Account is locked until: " + user.getAccountLockedUntil());
        }

        if (user.getForcePasswordChange()) {
            throw new PasswordChangeRequiredException("Password change required");
        }

        // AML/Compliance checks
        if (user.getAmlStatus() == AmlStatus.BLOCKED) {
            securityService.createAlert(user, SecurityAlertType.ACCOUNT_COMPROMISED,
                    AlertSeverity.CRITICAL, "Login attempt on AML blocked account", ipAddress);
            throw new ComplianceException("Account blocked due to compliance issues");
        }
    }

    private void handleFailedLogin(User user, String ipAddress, String reason) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
            securityService.createAlert(user, SecurityAlertType.ACCOUNT_LOCKED,
                    AlertSeverity.HIGH, "Account locked due to multiple failed attempts", ipAddress);
            emailService.sendAccountLockedEmail(user);
        } else if (user.getFailedLoginAttempts() >= 3) {
            securityService.createAlert(user, SecurityAlertType.MULTIPLE_FAILED_ATTEMPTS,
                    AlertSeverity.MEDIUM, "Multiple failed login attempts detected", ipAddress);
        }

        userRepository.save(user);
        auditService.log(AuditAction.FAILED_LOGIN, user, ipAddress, reason);
    }

    private void handleSuccessfulLogin(User user, String ipAddress, String userAgent) {
        // Reset failed attempts
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);

        // Location-based security check
        if (securityService.isUnusualLocation(user, ipAddress)) {
            securityService.createAlert(user, SecurityAlertType.UNUSUAL_LOCATION,
                    AlertSeverity.MEDIUM, "Login from unusual location", ipAddress);
            emailService.sendUnusualLocationAlert(user, ipAddress);
        }

        userRepository.save(user);
    }

    // ✅ Registration with Email Verification
    public RegistrationResponse register(RegistrationRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail().toLowerCase().trim();
        String ipAddress = clientIpAddress.getClientIpAddress(httpRequest);

        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        if (request.getNationalId() != null &&
                userRepository.existsByNationalId(request.getNationalId())) {
            throw new UserAlreadyExistsException("User with this national ID already exists");
        }

        // Validate password strength
        passwordService.validatePasswordStrength(request.getPassword());

        // Create user
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .nationalId(request.getNationalId())
                .passwordLastChanged(LocalDateTime.now())
                .passwordExpiresAt(LocalDateTime.now().plusDays(90))
                .verificationToken(tokenService.generateVerificationToken(ipAddress))
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .createdBy("SYSTEM")
                .build();

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user);

        // Audit log
        auditService.log(AuditAction.USER_REGISTRATION, user, ipAddress, "User registered");

        return RegistrationResponse.builder()
                .message("Registration successful. Please check your email for verification.")
                .email(email)
                .requiresEmailVerification(true)
                .build();
    }

    // Additional methods for password reset, MFA setup, etc.

    // Add this method to your AuthenticationService class

    /**
     * Refreshes access token using refresh token
     */
    public AuthenticationResponse refreshToken(String refreshToken, HttpServletRequest request) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidTokenException("Refresh token cannot be empty");
        }

        try {
            // Validate refresh token
            if (!jwtService.isValidToken(refreshToken)) {
                throw new InvalidTokenException("Invalid refresh token");
            }

            // Extract username from refresh token
            String username = jwtService.extractUsername(refreshToken);
            if (username == null) {
                throw new InvalidTokenException("Invalid refresh token format");
            }

            // Find user
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidTokenException("User not found"));

            // Check if user is active and verified
            if (!user.getIsActive() || !user.getIsVerified()) {
                throw new AccountNotActiveException("Account is not active");
            }

            // Check if account is locked
            if (user.getAccountLockedUntil() != null &&
                    LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
                throw new AccountLockedException("Account is temporarily locked");
            }

            // Check if refresh token exists in active sessions
            String sessionId = extractSessionIdFromToken(refreshToken);
            if (sessionId != null && !sessionService.isValidSession(user, sessionId)) {
                throw new InvalidTokenException("Session expired or invalid");
            }

            // Generate new tokens
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            // Update session with new tokens
            if (sessionId != null) {
                sessionService.updateSession(user, sessionId, newAccessToken, newRefreshToken);
            }

            // Extract device info
            DeviceInfo deviceInfo = deviceService.extractDeviceInfo(request);

            // Update last login info
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(deviceService.getClientIpAddress(request));
            userRepository.save(user);

            // Log the action
            auditService.logAction("TOKEN_REFRESHED", "User", user.getId(),
                    "Access token refreshed from IP: " + deviceInfo.getIpAddress());

            log.info("Token refreshed successfully for user: {}", user.getUsername());

            return AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getTokenExpirationTime())
                    .user(convertToUserDto(user))
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());

            if (e instanceof InvalidTokenException ||
                    e instanceof AccountNotActiveException ||
                    e instanceof AccountLockedException) {
                throw e;
            }

            throw new InvalidTokenException("Token refresh failed");
        }
    }

    /**
     * Extracts session ID from JWT token (if stored in claims)
     */
    private String extractSessionIdFromToken(String token) {
        try {
            return jwtService.extractClaim(token, claims -> claims.get("sessionId", String.class));
        } catch (Exception e) {
            return null;
        }
    }
}