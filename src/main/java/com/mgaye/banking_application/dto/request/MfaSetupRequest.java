package com.mgaye.banking_application.dto.request;

import com.mgaye.banking_application.entity.MfaMethod;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// MfaSetupRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MfaSetupRequest {

    @NotNull(message = "MFA method is required")
    private MfaMethod method;

    // For SMS MFA
    private String phoneNumber;

    // For Email MFA
    private String email;
}