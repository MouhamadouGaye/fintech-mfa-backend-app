package com.mgaye.banking_application.service;

import com.mgaye.banking_application.dto.TransactionDto;
import com.mgaye.banking_application.dto.TransferDto;
import com.mgaye.banking_application.entity.*;
import com.mgaye.banking_application.exception.AccountNotFoundException;
import com.mgaye.banking_application.exception.InsufficientFundsException;
import com.mgaye.banking_application.exception.TransactionNotFoundException;
import com.mgaye.banking_application.repository.AccountRepository;
import com.mgaye.banking_application.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public TransactionDto deposit(Long accountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        validateAccount(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setToAccount(account);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            // Update account balance
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            account.setAvailableBalance(newBalance);
            accountRepository.save(account);

            // Complete transaction
            transaction.setBalanceAfter(newBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditService.logAction("DEPOSIT", "Transaction", savedTransaction.getId(),
                    String.format("Deposit of %s to account %s", amount, account.getAccountNumber()));

            return convertToDto(savedTransaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Deposit failed: " + e.getMessage());
        }
    }

    public TransactionDto withdraw(Long accountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        validateAccount(account);

        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Check daily limit
        BigDecimal dailyWithdrawn = transactionRepository.getDailyWithdrawalAmount(accountId);
        if (dailyWithdrawn != null && dailyWithdrawn.add(amount).compareTo(account.getDailyLimit()) > 0) {
            throw new IllegalStateException("Daily withdrawal limit exceeded");
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setFromAccount(account);
        transaction.setBalanceBefore(account.getBalance());
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            // Update account balance
            BigDecimal newBalance = account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            account.setAvailableBalance(newBalance);
            accountRepository.save(account);

            // Complete transaction
            transaction.setBalanceAfter(newBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditService.logAction("WITHDRAWAL", "Transaction", savedTransaction.getId(),
                    String.format("Withdrawal of %s from account %s", amount, account.getAccountNumber()));

            return convertToDto(savedTransaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Withdrawal failed: " + e.getMessage());
        }
    }

    public TransactionDto transfer(TransferDto transferDto) {
        Account fromAccount = accountRepository.findByAccountNumber(transferDto.getFromAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumber(transferDto.getToAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

        validateAccount(fromAccount);
        validateAccount(toAccount);

        if (fromAccount.getAvailableBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(generateTransactionReference());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setAmount(transferDto.getAmount());
        transaction.setDescription(transferDto.getDescription());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setBalanceBefore(fromAccount.getBalance());
        transaction.setStatus(TransactionStatus.PENDING);

        try {
            // Update balances
            BigDecimal newFromBalance = fromAccount.getBalance().subtract(transferDto.getAmount());
            BigDecimal newToBalance = toAccount.getBalance().add(transferDto.getAmount());

            fromAccount.setBalance(newFromBalance);
            fromAccount.setAvailableBalance(newFromBalance);
            toAccount.setBalance(newToBalance);
            toAccount.setAvailableBalance(newToBalance);

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Complete transaction
            transaction.setBalanceAfter(newFromBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setProcessedAt(LocalDateTime.now());

            Transaction savedTransaction = transactionRepository.save(transaction);

            auditService.logAction("TRANSFER", "Transaction", savedTransaction.getId(),
                    String.format("Transfer of %s from %s to %s",
                            transferDto.getAmount(),
                            fromAccount.getAccountNumber(),
                            toAccount.getAccountNumber()));

            return convertToDto(savedTransaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);
            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        return convertToDto(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransactionByReference(String reference) {
        Transaction transaction = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));
        return convertToDto(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return transactionRepository.findByFromAccountOrToAccountOrderByCreatedAtDesc(account, account, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactionsByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findTransactionsByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByDateRange(Long accountId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return transactionRepository.findTransactionsByAccountAndDateRange(accountId, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateAccount(Account account) {
        if (!account.getIsActive()) {
            throw new IllegalStateException("Account is not active");
        }
        if (account.getIsFrozen()) {
            throw new IllegalStateException("Account is frozen");
        }
    }

    private String generateTransactionReference() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setTransactionReference(transaction.getTransactionReference());
        dto.setTransactionType(transaction.getTransactionType().toString());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus().toString());
        dto.setBalanceBefore(transaction.getBalanceBefore());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setFailureReason(transaction.getFailureReason());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setProcessedAt(transaction.getProcessedAt());

        if (transaction.getFromAccount() != null) {
            dto.setFromAccountNumber(transaction.getFromAccount().getAccountNumber());
            dto.setFromAccountId(transaction.getFromAccount().getId());
        }

        if (transaction.getToAccount() != null) {
            dto.setToAccountNumber(transaction.getToAccount().getAccountNumber());
            dto.setToAccountId(transaction.getToAccount().getId());
        }

        return dto;
    }
}