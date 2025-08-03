package com.mgaye.banking_application.service;

import com.mgaye.banking_application.dto.AccountDto;
import com.mgaye.banking_application.dto.CreateAccountDto;
import com.mgaye.banking_application.entity.Account;
import com.mgaye.banking_application.entity.AccountType;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.AccountNotFoundException;
import com.mgaye.banking_application.repository.AccountRepository;
import com.mgaye.banking_application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private CreateAccountDto createAccountDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");

        account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC1234567890");
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setDailyLimit(new BigDecimal("5000.00"));
        account.setIsActive(true);
        account.setIsFrozen(false);
        account.setUser(user);
        account.setCreatedAt(LocalDateTime.now());

        createAccountDto = new CreateAccountDto();
        createAccountDto.setUserId(1L);
        createAccountDto.setAccountType("CHECKING");
        createAccountDto.setDailyLimit(new BigDecimal("5000.00"));
    }

    @Test
    void createAccount_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // When
        AccountDto result = accountService.createAccount(createAccountDto);

        // Then
        assertNotNull(result);
        assertEquals("ACC1234567890", result.getAccountNumber());
        assertEquals("CHECKING", result.getAccountType());
        verify(accountRepository).save(any(Account.class));
        verify(auditService).logAction(eq("ACCOUNT_CREATED"), eq("Account"), eq(1L), anyString());
    }

    @Test
    void getAccountById_Success() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        AccountDto result = accountService.getAccountById(1L);

        // Then
        assertNotNull(result);
        assertEquals("ACC1234567890", result.getAccountNumber());
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AccountNotFoundException.class, () -> {
            accountService.getAccountById(1L);
        });
    }

    @Test
    void getAccountsByUserId_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByUserAndIsActiveTrue(user)).thenReturn(Arrays.asList(account));

        // When
        List<AccountDto> result = accountService.getAccountsByUserId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACC1234567890", result.get(0).getAccountNumber());
    }

    @Test
    void freezeAccount_Success() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        accountService.freezeAccount(1L);

        // Then
        assertTrue(account.getIsFrozen());
        verify(accountRepository).save(account);
        verify(auditService).logAction(eq("ACCOUNT_FROZEN"), eq("Account"), eq(1L), anyString());
    }

    @Test
    void unfreezeAccount_Success() {
        // Given
        account.setIsFrozen(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // When
        accountService.unfreezeAccount(1L);

        // Then
        assertFalse(account.getIsFrozen());
        verify(accountRepository).save(account);
        verify(auditService).logAction(eq("ACCOUNT_UNFROZEN"), eq("Account"), eq(1L), anyString());
    }
}
