package com.sportsaas.auth.domain.enums;

/**
 * Possible statuses for a user account.
 */
public enum UserStatus {
    PENDING,    // Account created but email not verified
    ACTIVE,     // Account active and verified
    SUSPENDED,  // Account suspended by admin
    LOCKED,     // Account locked due to failed login attempts
    DELETED     // Soft deleted
}
