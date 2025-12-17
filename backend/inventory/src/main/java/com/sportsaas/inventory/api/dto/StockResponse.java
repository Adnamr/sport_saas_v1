package com.sportsaas.inventory.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock response DTO.
 */
public record StockResponse(
    UUID id,
    UUID productId,
    String productName,
    String productSku,
    UUID warehouseId,
    String warehouseName,
    Integer quantity,
    Integer reservedQuantity,
    Integer availableQuantity,
    Integer alertThreshold,
    Boolean belowThreshold,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
