package com.mgaye.banking_application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAccountDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Account type is required")
    private String accountType; // SAVINGS, CHECKING, BUSINESS, FIXED_DEPOSIT

    @DecimalMin(value = "1000.0", message = "Daily limit must be at least 1000")
    private BigDecimal dailyLimit;
}
