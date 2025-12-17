package com.sportsaas.billing.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for InvoiceItem.
 */
public record InvoiceItemResponse(
    UUID id,
    UUID productId,
    String description,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal discountPercent,
    BigDecimal totalPrice,
    LocalDateTime createdAt
) {}
