package com.mgaye.banking_application.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VerifyMfaSetupRequest {
    @NotBlank(message = "MFA code is required")
    @Size(min = 6, max = 6)
    private String code;

    @NotNull(message = "Backup codes are required")
    @Size(min = 10, max = 10, message = "Must provide exactly 10 backup codes")
    private List<String> backupCodes;
}
