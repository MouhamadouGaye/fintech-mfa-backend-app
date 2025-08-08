package com.mgaye.banking_application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// BackupCodesResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupCodesResponse {

    private List<String> backupCodes;
    private String message;
    private LocalDateTime generatedAt;

    public BackupCodesResponse(List<String> codes) {
        this.backupCodes = codes;
        this.message = "Store these backup codes in a secure location. Each code can only be used once.";
        this.generatedAt = LocalDateTime.now();
    }
}
