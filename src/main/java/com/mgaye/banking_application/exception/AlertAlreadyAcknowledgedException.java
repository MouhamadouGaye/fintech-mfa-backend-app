package com.mgaye.banking_application.exception;

public class AlertAlreadyAcknowledgedException extends RuntimeException {
    public AlertAlreadyAcknowledgedException(String message) {
        super(message);
    }

}
