package com.sportsaas.inventory.api.controller;

import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.service.ProductService;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.api.dto.CreateStockRequest;
import com.sportsaas.inventory.api.dto.StockMovementRequest;
import com.sportsaas.inventory.api.dto.StockMovementResponse;
import com.sportsaas.inventory.api.dto.StockResponse;
import com.sportsaas.inventory.api.mapper.StockMapper;
import com.sportsaas.inventory.domain.entity.Stock;
import com.sportsaas.inventory.domain.entity.StockMovement;
import com.sportsaas.inventory.domain.entity.Warehouse;
import com.sportsaas.inventory.domain.enums.MovementType;
import com.sportsaas.inventory.domain.service.StockService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stock REST controller.
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Stock management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class StockController {

    private final StockService stockService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final StockMapper stockMapper;

    @GetMapping
    @Operation(summary = "List stock", description = "Get all stock for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<StockResponse>> list(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Stock> page = stockService.findByTenantId(tenantId, pageable);
        Page<StockResponse> responsePage = page.map(stockMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock by product", description = "Get stock levels for a product across all warehouses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockResponse>> getByProduct(@PathVariable UUID productId) {
        UUID tenantId = TenantContext.requireTenantId();
        List<Stock> stocks = stockService.findByProduct(tenantId, productId);
        List<StockResponse> response = stocks.stream()
            .map(stockMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get stock by warehouse", description = "Get all stock in a warehouse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<StockResponse>> getByWarehouse(
            @PathVariable UUID warehouseId,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Stock> page = stockService.findByWarehouse(tenantId, warehouseId, pageable);
        Page<StockResponse> responsePage = page.map(stockMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get low stock alerts", description = "Get products with stock below threshold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<StockResponse>> getAlerts(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Stock> page = stockService.findLowStock(tenantId, pageable);
        Page<StockResponse> responsePage = page.map(stockMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/availability/{productId}")
    @Operation(summary = "Check availability", description = "Check if product is available in requested quantity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "1") int quantity) {
        UUID tenantId = TenantContext.requireTenantId();
        Integer available = stockService.getAvailableQuantity(tenantId, productId);
        Integer total = stockService.getTotalQuantity(tenantId, productId);
        boolean isAvailable = stockService.checkAvailability(tenantId, productId, quantity);

        return ResponseEntity.ok(Map.of(
            "productId", productId,
            "requestedQuantity", quantity,
            "availableQuantity", available != null ? available : 0,
            "totalQuantity", total != null ? total : 0,
            "isAvailable", isAvailable
        ));
    }

    @PostMapping
    @Operation(summary = "Create stock entry", description = "Create a new stock entry for a product in a warehouse")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<StockResponse> create(@Valid @RequestBody CreateStockRequest request) {
        UUID tenantId = TenantContext.requireTenantId();

        Product product = productService.findById(request.productId())
            .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));

        Warehouse warehouse = warehouseService.findById(request.warehouseId())
            .orElseThrow(() -> new NotFoundException("Warehouse not found: " + request.warehouseId()));

        Stock stock = Stock.builder()
            .tenantId(tenantId)
            .product(product)
            .warehouse(warehouse)
            .quantity(request.initialQuantity() != null ? request.initialQuantity() : 0)
            .alertThreshold(request.alertThreshold() != null ? request.alertThreshold() : 5)
            .build();

        Stock saved = stockService.create(stock);
        return ResponseEntity.status(HttpStatus.CREATED).body(stockMapper.toResponse(saved));
    }

    @PostMapping("/movement")
    @Operation(summary = "Create stock movement", description = "Add, remove, or adjust stock")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody StockMovementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = null; // Would get from userDetails in real implementation

        StockMovement movement;
        switch (request.type()) {
            case IN:
                movement = stockService.addStock(
                    request.stockId(),
                    request.quantity(),
                    request.reason(),
                    request.reference(),
                    userId
                );
                break;
            case OUT:
                movement = stockService.removeStock(
                    request.stockId(),
                    request.quantity(),
                    request.reason(),
                    request.reference(),
                    userId
                );
                break;
            case ADJUSTMENT:
                movement = stockService.adjustStock(
                    request.stockId(),
                    request.newQuantity() != null ? request.newQuantity() : request.quantity(),
                    request.reason(),
                    userId
                );
                break;
            default:
                throw new IllegalArgumentException("Unsupported movement type: " + request.type());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(stockMapper.toMovementResponse(movement));
    }

    @GetMapping("/movements")
    @Operation(summary = "Get stock movements", description = "Get stock movement history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<StockMovementResponse>> getMovements(
            @RequestParam(required = false) MovementType type,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<StockMovement> page;

        if (type != null) {
            page = stockService.getMovementsByType(tenantId, type, pageable);
        } else {
            page = stockService.getMovements(tenantId, pageable);
        }

        Page<StockMovementResponse> responsePage = page.map(stockMapper::toMovementResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/movements/stock/{stockId}")
    @Operation(summary = "Get movements by stock", description = "Get movement history for a specific stock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<StockMovementResponse>> getMovementsByStock(
            @PathVariable UUID stockId,
            Pageable pageable) {
        Page<StockMovement> page = stockService.getMovementsByStock(stockId, pageable);
        Page<StockMovementResponse> responsePage = page.map(stockMapper::toMovementResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }
}
