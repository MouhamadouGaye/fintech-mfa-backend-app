package com.mgaye.banking_application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {
    private Long id;
    private String accountNumber;
    private String accountType;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal dailyLimit;
    private Boolean isActive;
    private Boolean isFrozen;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}