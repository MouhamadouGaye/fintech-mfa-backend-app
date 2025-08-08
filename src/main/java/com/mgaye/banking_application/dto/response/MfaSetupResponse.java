package com.mgaye.banking_application.dto.response;

import java.util.List;

import com.mgaye.banking_application.entity.MfaMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MfaSetupResponse {
    private String message;
    private String secret;
    private String qrCodeUrl;
    private List<String> backupCodes;
    private MfaMethod method;
    private Boolean requiresBackupCodes;
}