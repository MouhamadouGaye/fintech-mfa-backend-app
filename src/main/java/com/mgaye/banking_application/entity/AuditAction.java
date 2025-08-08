package com.mgaye.banking_application.entity;

public enum AuditAction {
    USER_LOGIN,
    USER_LOGOUT,
    USER_REGISTRATION,
    PASSWORD_CHANGE,
    MFA_ENABLED,
    MFA_DISABLED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    PROFILE_UPDATE,
    FAILED_LOGIN,
    DEVICE_TRUSTED,
    DEVICE_REMOVED
}
