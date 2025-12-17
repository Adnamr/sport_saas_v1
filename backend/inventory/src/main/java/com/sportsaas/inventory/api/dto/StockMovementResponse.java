package com.sportsaas.inventory.api.dto;

import com.sportsaas.inventory.domain.enums.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock movement response DTO.
 */
public record StockMovementResponse(
    UUID id,
    UUID stockId,
    UUID productId,
    String productName,
    UUID warehouseId,
    String warehouseName,
    MovementType type,
    Integer quantity,
    Integer quantityBefore,
    Integer quantityAfter,
    String reason,
    String reference,
    UUID userId,
    UUID orderId,
    LocalDateTime createdAt
) {}
