
// package com.mgaye.banking_application.entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.UpdateTimestamp;

// import java.time.LocalDateTime;
// import java.util.List;

// @Entity
// @Table(name = "users")
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class User {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(unique = true, nullable = false)
//     private String username;

//     @Column(unique = true, nullable = false)
//     private String email;

//     @Column(nullable = false)
//     private String password;

//     @Column(name = "first_name", nullable = false)
//     private String firstName;

//     @Column(name = "last_name", nullable = false)
//     private String lastName;

//     @Column(name = "phone_number")
//     private String phoneNumber;

//     @Column(name = "address")
//     private String address;

//     @Column(name = "date_of_birth")
//     private String dateOfBirth;

//     @Enumerated(EnumType.STRING)
//     private Role role = Role.CUSTOMER;

//     // ✅ Security Controls
//     @Column(name = "is_active")
//     private Boolean isActive = true;

//     @Column(name = "is_verified")
//     private Boolean isVerified = false;

//     @Column(name = "failed_login_attempts")
//     private Integer failedLoginAttempts = 0;

//     @Column(name = "account_locked_until")
//     private LocalDateTime accountLockedUntil;

//     @Column(name = "last_login_at")
//     private LocalDateTime lastLoginAt;

//     // ✅ MFA / TOTP
//     @Column(name = "is_mfa_enabled")
//     private Boolean isMfaEnabled = false;

//     @Column(name = "mfa_secret", length = 512)
//     private String mfaSecret;

//     // ✅ Email Verification
//     @Column(name = "verification_token")
//     private String verificationToken;

//     @Column(name = "verification_token_expiry")
//     private LocalDateTime verificationTokenExpiry;

//     // ✅ Password Policy
//     @Column(name = "password_last_changed")
//     private LocalDateTime passwordLastChanged;

//     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//     private List<PasswordHistory> passwordHistory;

//     // ✅ KYC / Compliance
//     @Column(name = "national_id")
//     private String nationalId;

//     @Column(name = "kyc_verified")
//     private Boolean kycVerified = false;

//     // ✅ Account Status Flags
//     @Column(name = "is_suspended")
//     private Boolean isSuspended = false;

//     @Column(name = "suspension_reason")
//     private String suspensionReason;

//     // ✅ Auditing
//     @CreationTimestamp
//     @Column(name = "created_at", updatable = false)
//     private LocalDateTime createdAt;

//     @UpdateTimestamp
//     @Column(name = "updated_at")
//     private LocalDateTime updatedAt;

//     // ✅ Associations
//     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//     private List<Account> accounts;

//     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//     private List<AuditLog> auditLogs;
// }

package com.mgaye.banking_application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_national_id", columnList = "national_id"),
        @Index(name = "idx_users_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Email as Primary Authentication (Username)
    @Column(unique = true, nullable = false, length = 320) // RFC 5321 limit
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(nullable = false)
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @JsonIgnore
    private String password;

    private String username;

    // ✅ Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Pattern(regexp = "^[a-zA-Z\\s'-]{1,100}$", message = "Invalid first name format")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    @Pattern(regexp = "^[a-zA-Z\\s'-]{1,100}$", message = "Invalid last name format")
    private String lastName;

    @Column(name = "phone_number", length = 20)
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "date_of_birth")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER;

    // ✅ Enhanced Security Controls
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45) // IPv6 support
    private String lastLoginIp;

    @Column(name = "login_source", length = 50)
    private String loginSource; // WEB, MOBILE, API

    // ✅ Enhanced MFA Support
    @Column(name = "is_mfa_enabled", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean isMfaEnabled = false;

    @Column(name = "mfa_secret", length = 512)
    @JsonIgnore
    private String mfaSecret;

    @Column(name = "backup_codes", length = 1000)
    @JsonIgnore
    private String backupCodes; // Encrypted JSON array

    @Column(name = "mfa_method")
    @Enumerated(EnumType.STRING)
    private MfaMethod mfaMethod;

    @Column(name = "mfa_enabled_at")
    private LocalDateTime mfaEnabledAt;

    // ✅ Email Verification & Communication
    @Column(name = "verification_token", length = 255)
    @JsonIgnore
    private String verificationToken;

    @Column(name = "verification_token_expiry")
    private LocalDateTime verificationTokenExpiry;

    @Column(name = "password_reset_token", length = 255)
    @JsonIgnore
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "email_notifications_enabled", nullable = false, columnDefinition = "Boolean Default true")
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(name = "sms_notifications_enabled", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean smsNotificationsEnabled = false;

    // ✅ Enhanced Password Policy
    @Column(name = "password_last_changed")
    private LocalDateTime passwordLastChanged;

    @Column(name = "password_expires_at")
    private LocalDateTime passwordExpiresAt;

    @Column(name = "force_password_change", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean forcePasswordChange = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordHistory> passwordHistory = new ArrayList<>();

    // ✅ Enhanced KYC & Compliance
    @Column(name = "national_id", unique = true, length = 50)
    private String nationalId;

    @Column(name = "kyc_status", nullable = false, columnDefinition = "VARCHAR(255) Default 'PENDING'")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_verified_at")
    private LocalDateTime kycVerifiedAt;

    @Column(name = "kyc_verified_by", length = 100)
    private String kycVerifiedBy;

    @Column(name = "risk_level", nullable = false, columnDefinition = "VARCHAR(20) Default 'LOW'")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;

    // ✅ Account Status & Compliance
    @Column(name = "is_suspended", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean isSuspended = false;

    @Column(name = "suspension_reason", length = 500)
    private String suspensionReason;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspended_by", length = 100)
    private String suspendedBy;

    @Column(name = "is_frozen", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean isFrozen = false;

    @Column(name = "freeze_reason", length = 500)
    private String freezeReason;

    // ✅ Compliance & Monitoring
    @Column(name = "aml_status", nullable = false, columnDefinition = "VARCHAR(255) Default 'CLEAR'")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AmlStatus amlStatus = AmlStatus.CLEAR;

    @Column(name = "sanctions_checked", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean sanctionsChecked = false;

    @Column(name = "sanctions_check_date")
    private LocalDateTime sanctionsCheckDate;

    @Column(name = "pep_status", nullable = false, columnDefinition = "Boolean Default false")
    @Builder.Default
    private Boolean pepStatus = false; // Politically Exposed Person

    // ✅ Session & Device Management
    @Column(name = "session_timeout_minutes", nullable = false, columnDefinition = "Integer Default 30")
    @Builder.Default
    private Integer sessionTimeoutMinutes = 30;

    @Column(name = "max_concurrent_sessions", nullable = false, columnDefinition = "Integer Default 3")
    @Builder.Default
    private Integer maxConcurrentSessions = 3;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSession> activeSessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrustedDevice> trustedDevices = new ArrayList<>();

    // ✅ Auditing & Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ✅ Banking Associations
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<LoginAttempt> loginAttempts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SecurityAlert> securityAlerts = new ArrayList<>();

    // ✅ Utility Methods
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isAccountExpired() {
        return passwordExpiresAt != null && passwordExpiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isCredentialsExpired() {
        return passwordLastChanged != null &&
                passwordLastChanged.plusDays(90).isBefore(LocalDateTime.now());
    }

    public boolean isEnabled() {
        return isActive && isVerified && !isSuspended && !isFrozen && !isAccountLocked();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public User orElseThrow(Object object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }
}