package com.sportsaas.catalog.api.controller;

import com.sportsaas.catalog.api.dto.CreateProductRequest;
import com.sportsaas.catalog.api.dto.ProductResponse;
import com.sportsaas.catalog.api.dto.UpdateProductRequest;
import com.sportsaas.catalog.api.mapper.ProductMapper;
import com.sportsaas.catalog.domain.entity.Category;
import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.enums.ProductStatus;
import com.sportsaas.catalog.domain.service.CategoryService;
import com.sportsaas.catalog.domain.service.ProductService;
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
 * Product REST controller.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    @GetMapping
    @Operation(summary = "List products", description = "Get all products for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ProductResponse>> list(
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) UUID categoryId,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Product> page;

        if (categoryId != null) {
            page = productService.findByCategory(tenantId, categoryId, pageable);
        } else if (status != null) {
            page = productService.findByTenantIdAndStatus(tenantId, status, pageable);
        } else {
            page = productService.findByTenantId(tenantId, pageable);
        }

        Page<ProductResponse> responsePage = page.map(productMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product", description = "Get a product by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponse> getById(@PathVariable UUID id) {
        Product product = productService.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found: " + id));

        // Increment view count
        productService.incrementViewCount(id);

        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug", description = "Get a product by slug")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        Product product = productService.findBySlug(slug)
            .orElseThrow(() -> new NotFoundException("Product not found: " + slug));

        productService.incrementViewCount(product.getId());

        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Get a product by SKU")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        Product product = productService.findBySku(sku)
            .orElseThrow(() -> new NotFoundException("Product not found: " + sku));
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name, description, SKU, or brand")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @RequestParam String q,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Product> page = productService.search(tenantId, q, pageable);
        Page<ProductResponse> responsePage = page.map(productMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/rentable")
    @Operation(summary = "Get rentable products", description = "Get all products available for rental")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductResponse>> getRentable() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Product> products = productService.findRentableProducts(tenantId);
        List<ProductResponse> response = products.stream()
            .map(productMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-viewed")
    @Operation(summary = "Get top viewed products", description = "Get most viewed products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductResponse>> getTopViewed(
            @RequestParam(defaultValue = "10") int limit) {
        UUID tenantId = TenantContext.requireTenantId();
        List<Product> products = productService.findTopViewed(tenantId, limit);
        List<ProductResponse> response = products.stream()
            .map(productMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-rented")
    @Operation(summary = "Get top rented products", description = "Get most rented products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProductResponse>> getTopRented(
            @RequestParam(defaultValue = "10") int limit) {
        UUID tenantId = TenantContext.requireTenantId();
        List<Product> products = productService.findTopRented(tenantId, limit);
        List<ProductResponse> response = products.stream()
            .map(productMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Create a new product")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Category category = categoryService.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));

        Product product = productMapper.toEntity(request);
        product.setTenantId(tenantId);
        product.setCategory(category);
        product.setStatus(ProductStatus.DRAFT);

        Product saved = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        Category category = categoryService.findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("Category not found: " + request.categoryId()));

        Product product = Product.builder()
            .name(request.name())
            .slug(request.slug())
            .sku(request.sku())
            .description(request.description())
            .shortDescription(request.shortDescription())
            .category(category)
            .condition(request.condition())
            .purchasePrice(request.purchasePrice())
            .rentalPriceDaily(request.rentalPriceDaily())
            .rentalPriceWeekly(request.rentalPriceWeekly())
            .rentalPriceMonthly(request.rentalPriceMonthly())
            .salePrice(request.salePrice())
            .depositAmount(request.depositAmount())
            .brand(request.brand())
            .model(request.model())
            .serialNumber(request.serialNumber())
            .size(request.size())
            .color(request.color())
            .weight(request.weight())
            .dimensions(request.dimensions())
            .isRentable(request.isRentable() != null ? request.isRentable() : true)
            .isSellable(request.isSellable() != null ? request.isSellable() : false)
            .requiresDeposit(request.requiresDeposit() != null ? request.requiresDeposit() : true)
            .minRentalDays(request.minRentalDays() != null ? request.minRentalDays() : 1)
            .maxRentalDays(request.maxRentalDays())
            .build();

        Product updated = productService.update(id, product);
        return ResponseEntity.ok(productMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate product", description = "Activate a product")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> activate(@PathVariable UUID id) {
        productService.activate(id);
        return ResponseEntity.ok(Map.of("message", "Product activated"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product", description = "Deactivate a product")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.ok(Map.of("message", "Product deactivated"));
    }

    @GetMapping("/check-slug")
    @Operation(summary = "Check slug availability", description = "Check if a product slug is available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkSlug(@RequestParam String slug) {
        UUID tenantId = TenantContext.requireTenantId();
        boolean available = productService.isSlugAvailable(slug, tenantId);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/check-sku")
    @Operation(summary = "Check SKU availability", description = "Check if a product SKU is available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkSku(@RequestParam String sku) {
        UUID tenantId = TenantContext.requireTenantId();
        boolean available = productService.isSkuAvailable(sku, tenantId);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
