package com.mgaye.banking_application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mgaye.banking_application.entity.KycStatus;
import com.mgaye.banking_application.entity.RiskLevel;
import com.mgaye.banking_application.entity.Role;
import com.mgaye.banking_application.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Role role;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isMfaEnabled;
    private KycStatus kycStatus;
    private RiskLevel riskLevel;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;
    private LocalDateTime passwordLastChanged;
    private Boolean requiresPasswordChange;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .isMfaEnabled(user.getIsMfaEnabled())
                .kycStatus(user.getKycStatus())
                .riskLevel(user.getRiskLevel())
                .lastLoginAt(user.getLastLoginAt())
                .lastLoginIp(user.getLastLoginIp())
                .createdAt(user.getCreatedAt())
                .passwordLastChanged(user.getPasswordLastChanged())
                .requiresPasswordChange(user.getForcePasswordChange())
                .build();
    }
}