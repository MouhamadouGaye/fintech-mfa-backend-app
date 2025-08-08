package com.mgaye.banking_application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private String type = "Bearer";
    private String username;
    // private String email;
    private String role;
    private Long userId;

    public JwtResponseDto(String token, String username,
            // String email,
            String role, Long userId) {
        this.token = token;
        this.username = username;
        // this.email = email;
        this.role = role;
        this.userId = userId;
    }
}