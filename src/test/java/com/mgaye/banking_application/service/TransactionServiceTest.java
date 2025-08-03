package com.mgaye.banking_application.service;

import com.mgaye.banking_application.dto.TransactionDto;
import com.mgaye.banking_application.entity.*;
import com.mgaye.banking_application.exception.AccountNotFoundException;
import com.mgaye.banking_application.exception.InsufficientFundsException;
import com.mgaye.banking_application.repository.AccountRepository;
import com.mgaye.banking_application.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    private Account account;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC1234567890");
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("1000.00"));
        account.setAvailableBalance(new BigDecimal("1000.00"));
        account.setDailyLimit(new BigDecimal("5000.00"));
        account.setIsActive(true);
        account.setIsFrozen(false);
        account.setUser(user);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionReference("TXN1234567890");
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void deposit_Success() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionDto result = transactionService.deposit(1L, new BigDecimal("100.00"), "Test deposit");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1100.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
        verify(auditService).logAction(eq("DEPOSIT"), eq("Transaction"), any(), anyString());
    }

    @Test
    void withdraw_Success() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.getDailyWithdrawalAmount(1L)).thenReturn(BigDecimal.ZERO);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // When
        TransactionDto result = transactionService.withdraw(1L, new BigDecimal("100.00"), "Test withdrawal");

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("900.00"), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
        verify(auditService).logAction(eq("WITHDRAWAL"), eq("Transaction"), any(), anyString());
    }

    @Test
    void withdraw_InsufficientFunds_ThrowsException() {
        // Given
        account.setBalance(new BigDecimal("50.00"));
        account.setAvailableBalance(new BigDecimal("50.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(InsufficientFundsException.class, () -> {
            transactionService.withdraw(1L, new BigDecimal("100.00"), "Test withdrawal");
        });
    }

    @Test
    void deposit_AccountNotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountNotFoundException.class, () -> {
            transactionService.deposit(1L, new BigDecimal("100.00"), "Test deposit");
        });
    }

    @Test
    void deposit_FrozenAccount_ThrowsException() {
        // Given
        account.setIsFrozen(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            transactionService.deposit(1L, new BigDecimal("100.00"), "Test deposit");
        });
    }
}