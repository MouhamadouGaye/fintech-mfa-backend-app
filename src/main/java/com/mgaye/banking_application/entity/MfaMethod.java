package com.mgaye.banking_application.entity;

// public enum MfaMethod {

//     TOTP,
//     SMS,
//     EMAIL,
//     HARDWARE_TOKEN
// }

public enum MfaMethod {
    TOTP("Time-based One-Time Password (Google Authenticator)"),
    SMS("SMS Text Message"),
    EMAIL("Email Verification");

    private final String description;

    MfaMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}