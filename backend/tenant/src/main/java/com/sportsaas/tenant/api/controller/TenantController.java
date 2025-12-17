package com.sportsaas.tenant.api.controller;

import com.sportsaas.common.dto.ApiResponse;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.tenant.api.dto.CreateTenantRequest;
import com.sportsaas.tenant.api.dto.TenantResponse;
import com.sportsaas.tenant.api.dto.UpdateTenantRequest;
import com.sportsaas.tenant.api.mapper.TenantMapper;
import com.sportsaas.tenant.domain.entity.Tenant;
import com.sportsaas.tenant.domain.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for tenant management.
 * Only accessible by SUPER_ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenants", description = "Tenant management (Super Admin only)")
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "List all tenants", description = "Returns paginated list of all tenants")
    public ResponseEntity<PageResponse<TenantResponse>> list(
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Tenant> page = tenantService.findAll(pageable);
        Page<TenantResponse> responsePage = page.map(tenantMapper::toResponse);

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get tenant by ID", description = "Returns a single tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> getById(
            @Parameter(description = "Tenant ID") @PathVariable UUID id) {

        Tenant tenant = tenantService.findById(id)
            .orElseThrow(() -> new NotFoundException("Tenant", id));

        return ResponseEntity.ok(ApiResponse.success(tenantMapper.toResponse(tenant)));
    }

    @GetMapping("/by-slug/{slug}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get tenant by slug", description = "Returns a single tenant by its slug")
    public ResponseEntity<ApiResponse<TenantResponse>> getBySlug(
            @Parameter(description = "Tenant slug") @PathVariable String slug) {

        Tenant tenant = tenantService.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException("Tenant with slug: " + slug));

        return ResponseEntity.ok(ApiResponse.success(tenantMapper.toResponse(tenant)));
    }

    @GetMapping("/check-slug")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Check slug availability", description = "Checks if a slug is available for use")
    public ResponseEntity<ApiResponse<Boolean>> checkSlugAvailability(
            @Parameter(description = "Slug to check") @RequestParam String slug) {

        boolean available = tenantService.isSlugAvailable(slug);
        return ResponseEntity.ok(ApiResponse.success(available));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new tenant", description = "Creates a new tenant in the system")
    public ResponseEntity<ApiResponse<TenantResponse>> create(
            @Valid @RequestBody CreateTenantRequest request) {

        Tenant tenant = tenantMapper.toEntity(request);
        Tenant created = tenantService.create(tenant);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(tenantMapper.toResponse(created), "Tenant created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update a tenant", description = "Updates an existing tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> update(
            @Parameter(description = "Tenant ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {

        Tenant tenant = tenantMapper.toEntity(
            new CreateTenantRequest(
                request.name(),
                request.slug(),
                request.contactEmail(),
                request.contactPhone(),
                request.address(),
                request.logoUrl(),
                request.subscriptionPlan()
            )
        );
        tenant.setMaxUsers(request.maxUsers());
        tenant.setMaxProducts(request.maxProducts());

        Tenant updated = tenantService.update(id, tenant);

        return ResponseEntity.ok(ApiResponse.success(tenantMapper.toResponse(updated), "Tenant updated successfully"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activate a tenant", description = "Activates a suspended or trial tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> activate(
            @Parameter(description = "Tenant ID") @PathVariable UUID id) {

        Tenant activated = tenantService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(tenantMapper.toResponse(activated), "Tenant activated"));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Suspend a tenant", description = "Suspends an active tenant")
    public ResponseEntity<ApiResponse<TenantResponse>> suspend(
            @Parameter(description = "Tenant ID") @PathVariable UUID id) {

        Tenant suspended = tenantService.suspend(id);
        return ResponseEntity.ok(ApiResponse.success(tenantMapper.toResponse(suspended), "Tenant suspended"));
    }
}
