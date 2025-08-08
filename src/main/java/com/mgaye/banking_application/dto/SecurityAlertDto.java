package com.mgaye.banking_application.dto;

import java.time.LocalDateTime;

import com.mgaye.banking_application.entity.AlertSeverity;
import com.mgaye.banking_application.entity.SecurityAlertType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityAlertDto {
    private Long id;
    private SecurityAlertType alertType;
    private AlertSeverity severity;
    private String message;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;
    private Boolean acknowledged;
    private Boolean resolved;
}