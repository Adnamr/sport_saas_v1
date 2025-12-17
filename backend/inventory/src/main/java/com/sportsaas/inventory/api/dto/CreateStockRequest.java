package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Create stock request DTO.
 */
public record CreateStockRequest(
    @NotNull(message = "Product ID is required")
    UUID productId,

    @NotNull(message = "Warehouse ID is required")
    UUID warehouseId,

    @Min(value = 0, message = "Initial quantity must be non-negative")
    Integer initialQuantity,

    @Min(value = 0, message = "Alert threshold must be non-negative")
    Integer alertThreshold
) {}
