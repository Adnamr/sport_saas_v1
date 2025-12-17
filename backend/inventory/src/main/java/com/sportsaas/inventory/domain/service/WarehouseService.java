package com.sportsaas.inventory.domain.service;

import com.sportsaas.inventory.domain.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Warehouse operations.
 */
public interface WarehouseService {

    Warehouse create(Warehouse warehouse);

    Warehouse update(UUID id, Warehouse warehouse);

    Optional<Warehouse> findById(UUID id);

    Page<Warehouse> findByTenantId(UUID tenantId, Pageable pageable);

    List<Warehouse> findActiveByTenantId(UUID tenantId);

    Optional<Warehouse> findDefaultByTenantId(UUID tenantId);

    Page<Warehouse> search(UUID tenantId, String search, Pageable pageable);

    void delete(UUID id);

    void activate(UUID id);

    void deactivate(UUID id);

    void setAsDefault(UUID id);

    long countByTenantId(UUID tenantId);
}
