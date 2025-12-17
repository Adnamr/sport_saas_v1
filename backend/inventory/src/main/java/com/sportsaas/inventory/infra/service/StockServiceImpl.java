package com.sportsaas.inventory.infra.service;

import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.entity.Stock;
import com.sportsaas.inventory.domain.entity.StockMovement;
import com.sportsaas.inventory.domain.enums.MovementType;
import com.sportsaas.inventory.domain.service.StockService;
import com.sportsaas.inventory.infra.repository.StockMovementRepository;
import com.sportsaas.inventory.infra.repository.StockRepository;
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
 * Implementation of StockService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public Stock create(Stock stock) {
        log.info("Creating stock for product {} in warehouse {}",
                 stock.getProduct() != null ? stock.getProduct().getId() : null,
                 stock.getWarehouse() != null ? stock.getWarehouse().getId() : null);
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public Stock update(UUID id, Stock stock) {
        Stock existing = stockRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + id));

        existing.setAlertThreshold(stock.getAlertThreshold());
        return stockRepository.save(existing);
    }

    @Override
    public Optional<Stock> findById(UUID id) {
        return stockRepository.findById(id);
    }

    @Override
    public Page<Stock> findByTenantId(UUID tenantId, Pageable pageable) {
        return stockRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public List<Stock> findByProduct(UUID tenantId, UUID productId) {
        return stockRepository.findByTenantIdAndProductId(tenantId, productId);
    }

    @Override
    public Optional<Stock> findByProductAndWarehouse(UUID tenantId, UUID productId, UUID warehouseId) {
        return stockRepository.findByTenantIdAndProductIdAndWarehouseId(tenantId, productId, warehouseId);
    }

    @Override
    public Page<Stock> findByWarehouse(UUID tenantId, UUID warehouseId, Pageable pageable) {
        return stockRepository.findByTenantIdAndWarehouseId(tenantId, warehouseId, pageable);
    }

    @Override
    public Page<Stock> findLowStock(UUID tenantId, Pageable pageable) {
        return stockRepository.findLowStockByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional
    public StockMovement addStock(UUID stockId, int quantity, String reason, String reference, UUID userId) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }

        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        int quantityBefore = stock.getQuantity();
        stock.setQuantity(quantityBefore + quantity);
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
            .stock(stock)
            .tenantId(stock.getTenantId())
            .type(MovementType.IN)
            .quantity(quantity)
            .quantityBefore(quantityBefore)
            .quantityAfter(stock.getQuantity())
            .reason(reason)
            .reference(reference)
            .userId(userId)
            .build();

        log.info("Added {} units to stock {}", quantity, stockId);
        return stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement removeStock(UUID stockId, int quantity, String reason, String reference, UUID userId) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }

        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        if (!stock.hasAvailable(quantity)) {
            throw new BusinessException("Insufficient available stock. Available: " + stock.getAvailableQuantity());
        }

        int quantityBefore = stock.getQuantity();
        stock.setQuantity(quantityBefore - quantity);
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
            .stock(stock)
            .tenantId(stock.getTenantId())
            .type(MovementType.OUT)
            .quantity(quantity)
            .quantityBefore(quantityBefore)
            .quantityAfter(stock.getQuantity())
            .reason(reason)
            .reference(reference)
            .userId(userId)
            .build();

        log.info("Removed {} units from stock {}", quantity, stockId);
        return stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement adjustStock(UUID stockId, int newQuantity, String reason, UUID userId) {
        if (newQuantity < 0) {
            throw new BusinessException("Quantity cannot be negative");
        }

        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        int quantityBefore = stock.getQuantity();
        int difference = newQuantity - quantityBefore;

        stock.setQuantity(newQuantity);
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
            .stock(stock)
            .tenantId(stock.getTenantId())
            .type(MovementType.ADJUSTMENT)
            .quantity(Math.abs(difference))
            .quantityBefore(quantityBefore)
            .quantityAfter(newQuantity)
            .reason(reason)
            .userId(userId)
            .build();

        log.info("Adjusted stock {} from {} to {}", stockId, quantityBefore, newQuantity);
        return stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement reserveStock(UUID stockId, int quantity, UUID orderId, UUID userId) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }

        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        if (!stock.hasAvailable(quantity)) {
            throw new BusinessException("Insufficient available stock. Available: " + stock.getAvailableQuantity());
        }

        int reservedBefore = stock.getReservedQuantity();
        stock.setReservedQuantity(reservedBefore + quantity);
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
            .stock(stock)
            .tenantId(stock.getTenantId())
            .type(MovementType.RESERVATION)
            .quantity(quantity)
            .quantityBefore(stock.getQuantity())
            .quantityAfter(stock.getQuantity())
            .reason("Stock reserved for order")
            .orderId(orderId)
            .userId(userId)
            .build();

        log.info("Reserved {} units from stock {} for order {}", quantity, stockId, orderId);
        return stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public StockMovement releaseStock(UUID stockId, int quantity, UUID orderId, UUID userId) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }

        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        if (stock.getReservedQuantity() < quantity) {
            throw new BusinessException("Cannot release more than reserved. Reserved: " + stock.getReservedQuantity());
        }

        int reservedBefore = stock.getReservedQuantity();
        stock.setReservedQuantity(reservedBefore - quantity);
        stockRepository.save(stock);

        StockMovement movement = StockMovement.builder()
            .stock(stock)
            .tenantId(stock.getTenantId())
            .type(MovementType.RELEASE)
            .quantity(quantity)
            .quantityBefore(stock.getQuantity())
            .quantityAfter(stock.getQuantity())
            .reason("Stock released from reservation")
            .orderId(orderId)
            .userId(userId)
            .build();

        log.info("Released {} units from stock {} for order {}", quantity, stockId, orderId);
        return stockMovementRepository.save(movement);
    }

    @Override
    public Page<StockMovement> getMovements(UUID tenantId, Pageable pageable) {
        return stockMovementRepository.findByTenantIdWithStock(tenantId, pageable);
    }

    @Override
    public Page<StockMovement> getMovementsByStock(UUID stockId, Pageable pageable) {
        return stockMovementRepository.findByStockId(stockId, pageable);
    }

    @Override
    public Page<StockMovement> getMovementsByType(UUID tenantId, MovementType type, Pageable pageable) {
        return stockMovementRepository.findByTenantIdAndType(tenantId, type, pageable);
    }

    @Override
    public Integer getAvailableQuantity(UUID tenantId, UUID productId) {
        return stockRepository.getAvailableQuantityByProduct(tenantId, productId);
    }

    @Override
    public Integer getTotalQuantity(UUID tenantId, UUID productId) {
        return stockRepository.getTotalQuantityByProduct(tenantId, productId);
    }

    @Override
    public boolean checkAvailability(UUID tenantId, UUID productId, int quantity) {
        Integer available = getAvailableQuantity(tenantId, productId);
        return available != null && available >= quantity;
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return stockRepository.countByTenantId(tenantId);
    }
}
