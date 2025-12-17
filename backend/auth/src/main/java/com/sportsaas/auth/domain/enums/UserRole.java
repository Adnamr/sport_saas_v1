package com.sportsaas.auth.domain.enums;

/**
 * User roles for RBAC.
 */
public enum UserRole {
    SUPER_ADMIN,    // Platform administrator (cross-tenant)
    TENANT_ADMIN,   // Tenant administrator
    MANAGER,        // Manager with elevated permissions
    EMPLOYEE,       // Regular employee
    CUSTOMER        // Customer/End user
}
