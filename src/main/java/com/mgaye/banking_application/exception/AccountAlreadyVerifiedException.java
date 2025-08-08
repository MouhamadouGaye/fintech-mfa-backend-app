package com.mgaye.banking_application.exception;

public class AccountAlreadyVerifiedException extends RuntimeException {
    public AccountAlreadyVerifiedException(String message) {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public AccountAlreadyVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }

}
