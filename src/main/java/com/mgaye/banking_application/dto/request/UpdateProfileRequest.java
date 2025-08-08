package com.mgaye.banking_application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    @Pattern(regexp = "^[a-zA-Z\\s'-]{1,100}$", message = "Invalid first name format")
    private String firstName;

    @Pattern(regexp = "^[a-zA-Z\\s'-]{1,100}$", message = "Invalid last name format")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    private String address;

    private Boolean emailNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
}