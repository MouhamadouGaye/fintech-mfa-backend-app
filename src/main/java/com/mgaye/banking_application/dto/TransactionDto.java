package com.mgaye.banking_application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private String transactionReference;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private String status;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}