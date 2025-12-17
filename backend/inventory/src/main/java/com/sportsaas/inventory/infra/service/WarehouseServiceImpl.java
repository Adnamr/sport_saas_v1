package com.sportsaas.inventory.infra.service;

import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.entity.Warehouse;
import com.sportsaas.inventory.domain.service.WarehouseService;
import com.sportsaas.inventory.infra.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of WarehouseService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public Warehouse create(Warehouse warehouse) {
        if (warehouseRepository.existsByNameAndTenantId(warehouse.getName(), warehouse.getTenantId())) {
            throw new ConflictException("Warehouse with name " + warehouse.getName() + " already exists");
        }

        // If this is the first warehouse or marked as default, ensure it's the only default
        if (Boolean.TRUE.equals(warehouse.getIsDefault())) {
            clearDefaultWarehouse(warehouse.getTenantId());
        } else if (warehouseRepository.countByTenantId(warehouse.getTenantId()) == 0) {
            // First warehouse is always default
            warehouse.setIsDefault(true);
        }

        log.info("Creating warehouse: {}", warehouse.getName());
        return warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public Warehouse update(UUID id, Warehouse warehouse) {
        Warehouse existing = warehouseRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + id));

        // Check name uniqueness if changed
        if (!existing.getName().equals(warehouse.getName()) &&
            warehouseRepository.existsByNameAndTenantId(warehouse.getName(), existing.getTenantId())) {
            throw new ConflictException("Warehouse with name " + warehouse.getName() + " already exists");
        }

        existing.setName(warehouse.getName());
        existing.setAddress(warehouse.getAddress());
        existing.setCity(warehouse.getCity());
        existing.setCountry(warehouse.getCountry());
        existing.setPostalCode(warehouse.getPostalCode());
        existing.setPhone(warehouse.getPhone());
        existing.setEmail(warehouse.getEmail());
        existing.setNotes(warehouse.getNotes());

        return warehouseRepository.save(existing);
    }

    @Override
    public Optional<Warehouse> findById(UUID id) {
        return warehouseRepository.findById(id);
    }

    @Override
    public Page<Warehouse> findByTenantId(UUID tenantId, Pageable pageable) {
        return warehouseRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public List<Warehouse> findActiveByTenantId(UUID tenantId) {
        return warehouseRepository.findByTenantIdAndActiveTrue(tenantId);
    }

    @Override
    public Optional<Warehouse> findDefaultByTenantId(UUID tenantId) {
        return warehouseRepository.findByTenantIdAndIsDefaultTrue(tenantId);
    }

    @Override
    public Page<Warehouse> search(UUID tenantId, String search, Pageable pageable) {
        return warehouseRepository.searchByTenantId(tenantId, search, pageable);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + id));

        if (Boolean.TRUE.equals(warehouse.getIsDefault())) {
            throw new ConflictException("Cannot delete default warehouse");
        }

        warehouseRepository.delete(warehouse);
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + id));

        warehouse.setActive(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + id));

        if (Boolean.TRUE.equals(warehouse.getIsDefault())) {
            throw new ConflictException("Cannot deactivate default warehouse");
        }

        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public void setAsDefault(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + id));

        clearDefaultWarehouse(warehouse.getTenantId());

        warehouse.setIsDefault(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return warehouseRepository.countByTenantId(tenantId);
    }

    private void clearDefaultWarehouse(UUID tenantId) {
        warehouseRepository.findByTenantIdAndIsDefaultTrue(tenantId)
            .ifPresent(defaultWarehouse -> {
                defaultWarehouse.setIsDefault(false);
                warehouseRepository.save(defaultWarehouse);
            });
    }
}
