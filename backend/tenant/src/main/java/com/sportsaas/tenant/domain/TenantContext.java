package com.sportsaas.tenant.domain;

import java.util.UUID;

/**
 * Holds the current tenant context using ThreadLocal.
 * This allows accessing the current tenant ID anywhere in the request processing chain.
 *
 * <p>Usage:
 * <pre>
 * // Set tenant at the beginning of request
 * TenantContext.setTenantId(tenantId);
 *
 * // Access tenant anywhere
 * UUID currentTenant = TenantContext.getTenantId();
 *
 * // Clear at the end of request
 * TenantContext.clear();
 * </pre>
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    /**
     * Sets the current tenant ID for this thread.
     *
     * @param tenantId the tenant ID to set
     */
    public static void setTenantId(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Gets the current tenant ID for this thread.
     *
     * @return the current tenant ID, or null if not set
     */
    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Gets the current tenant ID, throwing an exception if not set.
     *
     * @return the current tenant ID
     * @throws IllegalStateException if no tenant is set
     */
    public static UUID requireTenantId() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant ID set in context");
        }
        return tenantId;
    }

    /**
     * Checks if a tenant ID is set in the current context.
     *
     * @return true if a tenant ID is set
     */
    public static boolean hasTenantId() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Clears the tenant ID from the current thread.
     * Should always be called at the end of request processing to prevent memory leaks.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
