package com.mgaye.banking_application.exception;

public class PasswordReusedException extends RuntimeException {
    public PasswordReusedException(String message) {
        super(message);
    }
}