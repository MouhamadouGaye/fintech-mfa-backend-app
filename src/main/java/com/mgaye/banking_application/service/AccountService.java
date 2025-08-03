package com.mgaye.banking_application.service;

import com.mgaye.banking_application.dto.AccountDto;
import com.mgaye.banking_application.dto.CreateAccountDto;
import com.mgaye.banking_application.entity.Account;
import com.mgaye.banking_application.entity.AccountType;
import com.mgaye.banking_application.entity.User;
import com.mgaye.banking_application.exception.AccountNotFoundException;
import com.mgaye.banking_application.exception.InsufficientFundsException;
import com.mgaye.banking_application.exception.UserNotFoundException;
import com.mgaye.banking_application.repository.AccountRepository;
import com.mgaye.banking_application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountDto createAccount(CreateAccountDto createAccountDto) {
        User user = userRepository.findById(createAccountDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(AccountType.valueOf(createAccountDto.getAccountType()));
        account.setBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setDailyLimit(createAccountDto.getDailyLimit() != null ? createAccountDto.getDailyLimit()
                : new BigDecimal("5000.00"));
        account.setUser(user);

        Account savedAccount = accountRepository.save(account);

        auditService.logAction("ACCOUNT_CREATED", "Account", savedAccount.getId(),
                "Account created: " + savedAccount.getAccountNumber());

        return convertToDto(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        return convertToDto(account);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        return convertToDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return accountRepository.findByUserAndIsActiveTrue(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void freezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setIsFrozen(true);
        accountRepository.save(account);

        auditService.logAction("ACCOUNT_FROZEN", "Account", accountId,
                "Account frozen: " + account.getAccountNumber());
    }

    public void unfreezeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setIsFrozen(false);
        accountRepository.save(account);

        auditService.logAction("ACCOUNT_UNFROZEN", "Account", accountId,
                "Account unfrozen: " + account.getAccountNumber());
    }

    public void updateBalance(Long accountId, BigDecimal newBalance) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal oldBalance = account.getBalance();
        account.setBalance(newBalance);
        account.setAvailableBalance(newBalance);
        accountRepository.save(account);

        auditService.logAction("BALANCE_UPDATED", "Account", accountId,
                String.format("Balance updated from %s to %s", oldBalance, newBalance));
    }

    public boolean hasAccountBy(Long accountId) {
        return accountRepository.existsById(accountId);
    }

    public void validateAccountForTransaction(Account account, BigDecimal amount, String operationType) {
        if (!account.getIsActive()) {
            throw new IllegalStateException("Account is not active");
        }

        if (account.getIsFrozen()) {
            throw new IllegalStateException("Account is frozen");
        }

        if ("DEBIT".equals(operationType) && account.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }

    private String generateAccountNumber() {
        StringBuilder accountNumber = new StringBuilder("ACC");
        for (int i = 0; i < 10; i++) {
            accountNumber.append(secureRandom.nextInt(10));
        }

        // Ensure uniqueness
        while (accountRepository.existsByAccountNumber(accountNumber.toString())) {
            accountNumber = new StringBuilder("ACC");
            for (int i = 0; i < 10; i++) {
                accountNumber.append(secureRandom.nextInt(10));
            }
        }

        return accountNumber.toString();
    }

    private AccountDto convertToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setAccountType(account.getAccountType().toString());
        dto.setBalance(account.getBalance());
        dto.setAvailableBalance(account.getAvailableBalance());
        dto.setDailyLimit(account.getDailyLimit());
        dto.setIsActive(account.getIsActive());
        dto.setIsFrozen(account.getIsFrozen());
        dto.setUserId(account.getUser().getId());
        dto.setUserName(account.getUser().getFirstName() + " " + account.getUser().getLastName());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}