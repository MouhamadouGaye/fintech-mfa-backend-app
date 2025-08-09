package com.mgaye.banking_application.dto.response;

import java.util.List;

import com.mgaye.banking_application.dto.UserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private Long expiresIn;
    private String tokenType;
    private Boolean requiresMfa = false;
    private Boolean requiresDeviceVerification = false;
    private Boolean requiresPasswordChange = false;
    private String message;
    private List<String> warnings;
}