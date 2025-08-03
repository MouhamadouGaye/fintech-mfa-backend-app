package com.mgaye.banking_application.Controller;

import com.mgaye.banking_application.dto.ApiResponse;
import com.mgaye.banking_application.dto.TransactionDto;
import com.mgaye.banking_application.dto.TransferDto;
import com.mgaye.banking_application.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransferController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<TransactionDto>> transfer(@Valid @RequestBody TransferDto transferDto) {
        try {
            TransactionDto transaction = transactionService.transfer(transferDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Transfer completed successfully", transaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}