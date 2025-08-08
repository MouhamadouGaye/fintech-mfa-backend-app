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
public class TrustedDeviceDto {
    private Long id;
    private String deviceName;
    private String deviceType;
    private String browser;
    private String operatingSystem;
    private String location;
    private LocalDateTime trustedAt;
    private LocalDateTime lastUsedAt;
}