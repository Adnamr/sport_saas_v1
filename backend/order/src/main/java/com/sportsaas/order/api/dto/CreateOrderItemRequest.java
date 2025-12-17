package com.sportsaas.order.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating an order item.
 */
public record CreateOrderItemRequest(
    @NotNull(message = "Product ID is required")
    UUID productId,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    Integer quantity,

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    BigDecimal unitPrice,

    @Min(value = 0, message = "Discount percent cannot be negative")
    BigDecimal discountPercent,

    String notes
) {}
