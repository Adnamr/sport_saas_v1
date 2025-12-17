package com.sportsaas.inventory.infra.repository;

import com.sportsaas.inventory.domain.entity.Warehouse;
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
 * Repository for Warehouse entity.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Page<Warehouse> findByTenantId(UUID tenantId, Pageable pageable);

    List<Warehouse> findByTenantIdAndActiveTrue(UUID tenantId);

    Optional<Warehouse> findByTenantIdAndIsDefaultTrue(UUID tenantId);

    Optional<Warehouse> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);

    @Query("SELECT w FROM Warehouse w WHERE w.tenantId = :tenantId " +
           "AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(w.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Warehouse> searchByTenantId(@Param("tenantId") UUID tenantId,
                                     @Param("search") String search,
                                     Pageable pageable);

    long countByTenantId(UUID tenantId);
}
