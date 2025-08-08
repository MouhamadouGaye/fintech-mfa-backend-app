// GlobalExceptionHandler.java
package com.mgaye.banking_application.exception;

import com.mgaye.banking_application.dto.ApiResponse;
import com.mgaye.banking_application.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// @RestControllerAdvice
// public class GlobalExceptionHandler {

//     @ExceptionHandler(UserAlreadyExistsException.class)
//     public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExists(UserAlreadyExistsException ex,
//             WebRequest request) {
//         return ResponseEntity.status(HttpStatus.CONFLICT)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(UserNotFoundException.class)
//     public ResponseEntity<ApiResponse<Object>> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(AccountNotFoundException.class)
//     public ResponseEntity<ApiResponse<Object>> handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(InsufficientFundsException.class)
//     public ResponseEntity<ApiResponse<Object>> handleInsufficientFunds(InsufficientFundsException ex,
//             WebRequest request) {
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(TransactionNotFoundException.class)
//     public ResponseEntity<ApiResponse<Object>> handleTransactionNotFound(TransactionNotFoundException ex,
//             WebRequest request) {
//         return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(BadCredentialsException.class)
//     public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                 .body(ApiResponse.error("Invalid username or password"));
//     }

//     @ExceptionHandler(DisabledException.class)
//     public ResponseEntity<ApiResponse<Object>> handleDisabled(DisabledException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                 .body(ApiResponse.error("Account is disabled"));
//     }

//     @ExceptionHandler(AccessDeniedException.class)
//     public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                 .body(ApiResponse.error("Access denied"));
//     }

//     @ExceptionHandler(MethodArgumentNotValidException.class)
//     public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex,
//             WebRequest request) {
//         List<String> details = new ArrayList<>();
//         for (FieldError error : ex.getBindingResult().getFieldErrors()) {
//             details.add(error.getField() + ": " + error.getDefaultMessage());
//         }

//         ErrorResponseDto errorResponse = new ErrorResponseDto(
//                 HttpStatus.BAD_REQUEST.value(),
//                 "Validation Failed",
//                 "Input validation error",
//                 details,
//                 request.getDescription(false).replace("uri=", ""),
//                 LocalDateTime.now());

//         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//     }

//     @ExceptionHandler(IllegalStateException.class)
//     public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(IllegalArgumentException.class)
//     public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
//         return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                 .body(ApiResponse.error(ex.getMessage()));
//     }

//     @ExceptionHandler(Exception.class)
//     public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex, WebRequest request) {
//         ErrorResponseDto errorResponse = new ErrorResponseDto(
//                 HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                 "Internal Server Error",
//                 "An unexpected error occurred",
//                 List.of(ex.getMessage()),
//                 request.getDescription(false).replace("uri=", ""),
//                 LocalDateTime.now());

//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//     }
// }

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .error("INVALID_CREDENTIALS")
                        .message("Invalid email or password")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotVerified(AccountNotVerifiedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .error("ACCOUNT_NOT_VERIFIED")
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(AccountLockedException e) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ErrorResponse.builder()
                        .error("ACCOUNT_LOCKED")
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyAttempts(TooManyAttemptsException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.builder()
                        .error("TOO_MANY_ATTEMPTS")
                        .message(e.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .error("VALIDATION_ERROR")
                        .message("Validation failed")
                        .details(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}