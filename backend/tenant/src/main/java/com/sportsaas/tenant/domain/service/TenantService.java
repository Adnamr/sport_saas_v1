package com.sportsaas.tenant.domain.service;

import com.sportsaas.tenant.domain.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for tenant management operations.
 */
public interface TenantService {

    /**
     * Creates a new tenant.
     *
     * @param tenant the tenant to create
     * @return the created tenant
     */
    Tenant create(Tenant tenant);

    /**
     * Updates an existing tenant.
     *
     * @param id the tenant ID
     * @param tenant the updated tenant data
     * @return the updated tenant
     */
    Tenant update(UUID id, Tenant tenant);

    /**
     * Finds a tenant by ID.
     *
     * @param id the tenant ID
     * @return the tenant if found
     */
    Optional<Tenant> findById(UUID id);

    /**
     * Finds a tenant by slug.
     *
     * @param slug the tenant slug
     * @return the tenant if found
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Gets all tenants with pagination.
     *
     * @param pageable pagination parameters
     * @return page of tenants
     */
    Page<Tenant> findAll(Pageable pageable);

    /**
     * Checks if a slug is available.
     *
     * @param slug the slug to check
     * @return true if available
     */
    boolean isSlugAvailable(String slug);

    /**
     * Activates a tenant.
     *
     * @param id the tenant ID
     * @return the activated tenant
     */
    Tenant activate(UUID id);

    /**
     * Suspends a tenant.
     *
     * @param id the tenant ID
     * @return the suspended tenant
     */
    Tenant suspend(UUID id);

    /**
     * Validates that a tenant exists and is active.
     *
     * @param tenantId the tenant ID to validate
     * @throws com.sportsaas.common.exception.NotFoundException if tenant not found
     * @throws com.sportsaas.common.exception.ForbiddenException if tenant not active
     */
    void validateTenantAccess(UUID tenantId);
}
