package com.mgaye.banking_application.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegistrationResponse {

    private String message;
    private String userId;
    private String email;
    private boolean requiresEmailVerification;
    private boolean requiresPhoneVerification;
    private LocalDateTime registeredAt;

    public boolean getRequiresEmailVerification() {
        return requiresEmailVerification;
    }

    public void setRequiresEmailVerification(boolean requiresEmailVerification) {
        this.requiresEmailVerification = requiresEmailVerification;
    }
}