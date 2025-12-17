package com.sportsaas.order.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for OrderItem.
 */
public record OrderItemResponse(
    UUID id,
    UUID productId,
    String productName,
    String productSku,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal discountPercent,
    BigDecimal effectiveUnitPrice,
    BigDecimal totalPrice,
    UUID reservationId,
    String notes,
    LocalDateTime createdAt
) {}
