package com.mgaye.banking_application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 128)
    private String password;

    @Size(min = 6, max = 6, message = "MFA code must be 6 digits")
    @Pattern(regexp = "\\d{6}", message = "MFA code must contain only digits")
    private String mfaCode;

    @Size(min = 6, max = 8, message = "Device verification code must be 6-8 characters")
    private String deviceVerificationCode;

    private Boolean trustDevice = false;
}