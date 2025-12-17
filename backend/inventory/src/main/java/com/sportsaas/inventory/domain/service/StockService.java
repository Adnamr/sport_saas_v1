package com.sportsaas.inventory.domain.service;

import com.sportsaas.inventory.domain.entity.Stock;
import com.sportsaas.inventory.domain.entity.StockMovement;
import com.sportsaas.inventory.domain.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Stock operations.
 */
public interface StockService {

    Stock create(Stock stock);

    Stock update(UUID id, Stock stock);

    Optional<Stock> findById(UUID id);

    Page<Stock> findByTenantId(UUID tenantId, Pageable pageable);

    List<Stock> findByProduct(UUID tenantId, UUID productId);

    Optional<Stock> findByProductAndWarehouse(UUID tenantId, UUID productId, UUID warehouseId);

    Page<Stock> findByWarehouse(UUID tenantId, UUID warehouseId, Pageable pageable);

    Page<Stock> findLowStock(UUID tenantId, Pageable pageable);

    // Stock movements
    StockMovement addStock(UUID stockId, int quantity, String reason, String reference, UUID userId);

    StockMovement removeStock(UUID stockId, int quantity, String reason, String reference, UUID userId);

    StockMovement adjustStock(UUID stockId, int newQuantity, String reason, UUID userId);

    StockMovement reserveStock(UUID stockId, int quantity, UUID orderId, UUID userId);

    StockMovement releaseStock(UUID stockId, int quantity, UUID orderId, UUID userId);

    // Movement history
    Page<StockMovement> getMovements(UUID tenantId, Pageable pageable);

    Page<StockMovement> getMovementsByStock(UUID stockId, Pageable pageable);

    Page<StockMovement> getMovementsByType(UUID tenantId, MovementType type, Pageable pageable);

    // Availability
    Integer getAvailableQuantity(UUID tenantId, UUID productId);

    Integer getTotalQuantity(UUID tenantId, UUID productId);

    boolean checkAvailability(UUID tenantId, UUID productId, int quantity);

    long countByTenantId(UUID tenantId);
}
