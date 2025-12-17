package com.sportsaas.inventory.api.controller;

import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.api.dto.CreateWarehouseRequest;
import com.sportsaas.inventory.api.dto.UpdateWarehouseRequest;
import com.sportsaas.inventory.api.dto.WarehouseResponse;
import com.sportsaas.inventory.api.mapper.WarehouseMapper;
import com.sportsaas.inventory.domain.entity.Warehouse;
import com.sportsaas.inventory.domain.service.WarehouseService;
import com.sportsaas.tenant.domain.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Warehouse REST controller.
 */
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouses", description = "Warehouse management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;

    @GetMapping
    @Operation(summary = "List warehouses", description = "Get all warehouses for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<WarehouseResponse>> list(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Warehouse> page = warehouseService.findByTenantId(tenantId, pageable);
        Page<WarehouseResponse> responsePage = page.map(warehouseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/active")
    @Operation(summary = "List active warehouses", description = "Get all active warehouses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WarehouseResponse>> listActive() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Warehouse> warehouses = warehouseService.findActiveByTenantId(tenantId);
        List<WarehouseResponse> response = warehouses.stream()
            .map(warehouseMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/default")
    @Operation(summary = "Get default warehouse", description = "Get the default warehouse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WarehouseResponse> getDefault() {
        UUID tenantId = TenantContext.requireTenantId();
        Warehouse warehouse = warehouseService.findDefaultByTenantId(tenantId)
            .orElseThrow(() -> new NotFoundException("No default warehouse found"));
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse", description = "Get a warehouse by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WarehouseResponse> getById(@PathVariable UUID id) {
        Warehouse warehouse = warehouseService.findById(id)
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + id));
        return ResponseEntity.ok(warehouseMapper.toResponse(warehouse));
    }

    @GetMapping("/search")
    @Operation(summary = "Search warehouses", description = "Search warehouses by name or city")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<WarehouseResponse>> search(
            @RequestParam String q,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Warehouse> page = warehouseService.search(tenantId, q, pageable);
        Page<WarehouseResponse> responsePage = page.map(warehouseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @PostMapping
    @Operation(summary = "Create warehouse", description = "Create a new warehouse")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody CreateWarehouseRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Warehouse warehouse = warehouseMapper.toEntity(request);
        warehouse.setTenantId(tenantId);

        Warehouse saved = warehouseService.create(warehouse);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update warehouse", description = "Update an existing warehouse")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<WarehouseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        Warehouse warehouse = Warehouse.builder()
            .name(request.name())
            .address(request.address())
            .city(request.city())
            .country(request.country())
            .postalCode(request.postalCode())
            .phone(request.phone())
            .email(request.email())
            .notes(request.notes())
            .build();

        Warehouse updated = warehouseService.update(id, warehouse);
        return ResponseEntity.ok(warehouseMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete warehouse", description = "Delete a warehouse")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate warehouse", description = "Activate a warehouse")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> activate(@PathVariable UUID id) {
        warehouseService.activate(id);
        return ResponseEntity.ok(Map.of("message", "Warehouse activated"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate warehouse", description = "Deactivate a warehouse")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable UUID id) {
        warehouseService.deactivate(id);
        return ResponseEntity.ok(Map.of("message", "Warehouse deactivated"));
    }

    @PostMapping("/{id}/set-default")
    @Operation(summary = "Set as default", description = "Set warehouse as default")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Map<String, String>> setAsDefault(@PathVariable UUID id) {
        warehouseService.setAsDefault(id);
        return ResponseEntity.ok(Map.of("message", "Warehouse set as default"));
    }
}
