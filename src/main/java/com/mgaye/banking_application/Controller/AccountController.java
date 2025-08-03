package com.mgaye.banking_application.Controller;

import com.mgaye.banking_application.dto.*;
import com.mgaye.banking_application.service.AccountService;
import com.mgaye.banking_application.service.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #createAccountDto.userId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            @Valid @RequestBody CreateAccountDto createAccountDto) {
        try {
            AccountDto account = accountService.createAccount(createAccountDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Account created successfully", account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @accountService.isAccountOwner(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<AccountDto>> getAccount(@PathVariable Long id) {
        try {
            AccountDto account = accountService.getAccountById(id);
            return ResponseEntity.ok(ApiResponse.success(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/number/{accountNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountDto>> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            AccountDto account = accountService.getAccountByNumber(accountNumber);
            return ResponseEntity.ok(ApiResponse.success(account));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and #userId == authentication.principal.id)")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getUserAccounts(@PathVariable Long userId) {
        try {
            List<AccountDto> accounts = accountService.getAccountsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(accounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-accounts")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getMyAccounts(Authentication authentication) {
        try {
            UserDetailsServiceImpl.UserDetailsImpl userDetails = (UserDetailsServiceImpl.UserDetailsImpl) authentication
                    .getPrincipal();
            List<AccountDto> accounts = accountService.getAccountsByUserId(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(accounts));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> freezeAccount(@PathVariable Long id) {
        try {
            accountService.freezeAccount(id);
            return ResponseEntity.ok(ApiResponse.success("Account frozen successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/unfreeze")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> unfreezeAccount(@PathVariable Long id) {
        try {
            accountService.unfreezeAccount(id);
            return ResponseEntity.ok(ApiResponse.success("Account unfrozen successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}