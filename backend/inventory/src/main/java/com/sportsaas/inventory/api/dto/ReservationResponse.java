package com.sportsaas.inventory.api.dto;

import com.sportsaas.inventory.domain.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reservation response DTO.
 */
public record ReservationResponse(
    UUID id,
    UUID stockId,
    UUID productId,
    String productName,
    UUID warehouseId,
    String warehouseName,
    UUID orderId,
    Integer quantity,
    ReservationStatus status,
    LocalDateTime expiresAt,
    LocalDateTime confirmedAt,
    LocalDateTime releasedAt,
    String notes,
    LocalDateTime createdAt
) {}
