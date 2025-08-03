package com.mgaye.banking_application.Controller;

import com.mgaye.banking_application.dto.ApiResponse;
import com.mgaye.banking_application.dto.UserResponseDto;
import com.mgaye.banking_application.entity.AuditLog;
import com.mgaye.banking_application.service.AccountService;
import com.mgaye.banking_application.service.AuditService;
import com.mgaye.banking_application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final AuditService auditService;

    // User Management
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        try {
            List<UserResponseDto> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        try {
            UserResponseDto user = userService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}/lock")
    public ResponseEntity<ApiResponse<String>> lockUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            userService.lockUser(id, hours);
            return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/unlock")
    public ResponseEntity<ApiResponse<String>> unlockUser(@PathVariable Long id) {
        try {
            userService.unlockUser(id);
            return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Account Management
    @PutMapping("/accounts/{id}/balance")
    public ResponseEntity<ApiResponse<String>> updateAccountBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal newBalance) {
        try {
            accountService.updateBalance(id, newBalance);
            return ResponseEntity.ok(ApiResponse.success("Account balance updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Audit Management
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AuditLog> auditLogs = auditService.getAuditLogs(startDate, endDate, pageable);
            return ResponseEntity.ok(ApiResponse.success(auditLogs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
