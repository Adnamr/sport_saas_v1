package com.sportsaas.inventory.api.dto;

import com.sportsaas.inventory.domain.enums.MovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Stock movement request DTO.
 */
public record StockMovementRequest(
    @NotNull(message = "Stock ID is required")
    UUID stockId,

    @NotNull(message = "Movement type is required")
    MovementType type,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    @Size(max = 500, message = "Reason must be at most 500 characters")
    String reason,

    @Size(max = 100, message = "Reference must be at most 100 characters")
    String reference,

    Integer newQuantity // For adjustments only
) {}
