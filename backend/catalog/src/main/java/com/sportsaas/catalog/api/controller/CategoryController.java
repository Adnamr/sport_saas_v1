package com.sportsaas.catalog.api.controller;

import com.sportsaas.catalog.api.dto.CategoryResponse;
import com.sportsaas.catalog.api.dto.CreateCategoryRequest;
import com.sportsaas.catalog.api.dto.UpdateCategoryRequest;
import com.sportsaas.catalog.api.mapper.CategoryMapper;
import com.sportsaas.catalog.domain.entity.Category;
import com.sportsaas.catalog.domain.service.CategoryService;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
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
 * Category REST controller.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    @Operation(summary = "List categories", description = "Get all categories for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<CategoryResponse>> list(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Category> page = categoryService.findByTenantId(tenantId, pageable);
        Page<CategoryResponse> responsePage = page.map(categoryMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Get hierarchical category tree")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> getTree() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Category> rootCategories = categoryService.findRootCategories(tenantId);
        List<CategoryResponse> response = rootCategories.stream()
            .map(categoryMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category", description = "Get a category by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        Category category = categoryService.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found: " + id));
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Get a category by slug")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        Category category = categoryService.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException("Category not found: " + slug));
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Get child categories of a parent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CategoryResponse>> getChildren(@PathVariable UUID id) {
        List<Category> children = categoryService.findByParentId(id);
        List<CategoryResponse> response = children.stream()
            .map(categoryMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Search categories by name or description")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<CategoryResponse>> search(
            @RequestParam String q,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Category> page = categoryService.search(tenantId, q, pageable);
        Page<CategoryResponse> responsePage = page.map(categoryMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Category category = categoryMapper.toEntity(request);
        category.setTenantId(tenantId);

        if (request.parentId() != null) {
            Category parent = categoryService.findById(request.parentId())
                .orElseThrow(() -> new NotFoundException("Parent category not found: " + request.parentId()));
            category.setParent(parent);
        }

        Category saved = categoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        Category category = Category.builder()
            .name(request.name())
            .slug(request.slug())
            .description(request.description())
            .imageUrl(request.imageUrl())
            .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
            .build();

        if (request.parentId() != null) {
            Category parent = categoryService.findById(request.parentId())
                .orElseThrow(() -> new NotFoundException("Parent category not found: " + request.parentId()));
            category.setParent(parent);
        }

        Category updated = categoryService.update(id, category);
        return ResponseEntity.ok(categoryMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate category", description = "Activate a category")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> activate(@PathVariable UUID id) {
        categoryService.activate(id);
        return ResponseEntity.ok(Map.of("message", "Category activated"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate category", description = "Deactivate a category")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable UUID id) {
        categoryService.deactivate(id);
        return ResponseEntity.ok(Map.of("message", "Category deactivated"));
    }

    @GetMapping("/check-slug")
    @Operation(summary = "Check slug availability", description = "Check if a category slug is available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkSlug(@RequestParam String slug) {
        UUID tenantId = TenantContext.requireTenantId();
        boolean available = categoryService.isSlugAvailable(slug, tenantId);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
