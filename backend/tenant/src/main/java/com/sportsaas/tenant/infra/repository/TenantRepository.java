package com.sportsaas.tenant.infra.repository;

import com.sportsaas.tenant.domain.entity.Tenant;
import com.sportsaas.tenant.domain.enums.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Tenant entities.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Finds a tenant by its unique slug.
     *
     * @param slug the tenant slug
     * @return the tenant if found
     */
    Optional<Tenant> findBySlug(String slug);

    /**
     * Checks if a tenant with the given slug exists.
     *
     * @param slug the slug to check
     * @return true if exists
     */
    boolean existsBySlug(String slug);

    /**
     * Finds all tenants by status.
     *
     * @param status the tenant status
     * @param pageable pagination parameters
     * @return page of tenants
     */
    Page<Tenant> findByStatus(TenantStatus status, Pageable pageable);

    /**
     * Finds all active tenants (ACTIVE or TRIAL status).
     *
     * @return list of active tenants
     */
    @Query("SELECT t FROM Tenant t WHERE t.status IN ('ACTIVE', 'TRIAL')")
    List<Tenant> findAllActive();

    /**
     * Finds tenants with expired trials.
     *
     * @return list of tenants with expired trials
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'TRIAL' AND t.trialEndsAt < CURRENT_TIMESTAMP")
    List<Tenant> findExpiredTrials();

    /**
     * Finds tenants by contact email.
     *
     * @param email the contact email
     * @return the tenant if found
     */
    Optional<Tenant> findByContactEmail(String email);

    /**
     * Counts tenants by status.
     *
     * @param status the status to count
     * @return count of tenants
     */
    long countByStatus(TenantStatus status);

    /**
     * Searches tenants by name or slug.
     *
     * @param search the search term
     * @param pageable pagination parameters
     * @return page of matching tenants
     */
    @Query("SELECT t FROM Tenant t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Tenant> search(@Param("search") String search, Pageable pageable);
}
