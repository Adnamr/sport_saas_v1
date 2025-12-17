package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Create reservation request DTO.
 */
public record CreateReservationRequest(
    @NotNull(message = "Stock ID is required")
    UUID stockId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    UUID orderId,

    @Min(value = 1, message = "Expiration minutes must be at least 1")
    Integer expirationMinutes // Default: 30 minutes
) {}
