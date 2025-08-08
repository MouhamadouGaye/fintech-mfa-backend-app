package com.mgaye.banking_application.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginAttemptDto {
    private Long id;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime attemptTime;
    private Boolean success;
    private String failureReason;
    private String location;
    private Boolean blocked;
}