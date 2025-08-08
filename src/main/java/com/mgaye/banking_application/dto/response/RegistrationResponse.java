package com.mgaye.banking_application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

    private String message;
    private String userId;
    private boolean requiresEmailVerification;
    private boolean requiresPhoneVerification;
    private LocalDateTime registeredAt;
}