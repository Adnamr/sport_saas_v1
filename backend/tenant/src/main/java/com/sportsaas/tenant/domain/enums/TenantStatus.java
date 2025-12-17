package com.sportsaas.tenant.domain.enums;

/**
 * Status of a tenant in the system.
 */
public enum TenantStatus {
    /**
     * Tenant is in trial period.
     */
    TRIAL,

    /**
     * Tenant is active with a valid subscription.
     */
    ACTIVE,

    /**
     * Tenant is suspended (e.g., payment issues).
     */
    SUSPENDED,

    /**
     * Tenant has been cancelled.
     */
    CANCELLED
}
