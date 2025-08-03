package com.mgaye.banking_application.Controller;

import com.mgaye.banking_application.dto.*;
import com.mgaye.banking_application.service.TransactionService;
import com.mgaye.banking_application.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionDto>> deposit(@Valid @RequestBody DepositDto depositDto) {
        try {
            TransactionDto transaction = transactionService.deposit(
                    depositDto.getAccountId(),
                    depositDto.getAmount(),
                    depositDto.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Deposit completed successfully", transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionDto>> withdraw(@Valid @RequestBody WithdrawalDto withdrawalDto) {
        try {
            TransactionDto transaction = transactionService.withdraw(
                    withdrawalDto.getAccountId(),
                    withdrawalDto.getAmount(),
                    withdrawalDto.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Withdrawal completed successfully", transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransaction(@PathVariable Long id) {
        try {
            TransactionDto transaction = transactionService.getTransactionById(id);
            return ResponseEntity.ok(ApiResponse.success(transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransactionByReference(@PathVariable String reference) {
        try {
            TransactionDto transaction = transactionService.getTransactionByReference(reference);
            return ResponseEntity.ok(ApiResponse.success(transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<TransactionDto> transactions = transactionService.getTransactionsByAccountId(accountId, pageable);
            return ResponseEntity.ok(ApiResponse.success(transactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            UserDetailsServiceImpl.UserDetailsImpl userDetails = (UserDetailsServiceImpl.UserDetailsImpl) authentication
                    .getPrincipal();

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<TransactionDto> transactions = transactionService.getTransactionsByUserId(userDetails.getId(),
                    pageable);
            return ResponseEntity.ok(ApiResponse.success(transactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/account/{accountId}/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getTransactionsByDateRange(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<TransactionDto> transactions = transactionService.getTransactionsByDateRange(accountId, startDate,
                    endDate);
            return ResponseEntity.ok(ApiResponse.success(transactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}