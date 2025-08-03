package com.mgaye.banking_application.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String dateOfBirth;
    private String role;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}