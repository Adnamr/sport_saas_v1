package com.sportsaas.order.api.dto;

import com.sportsaas.order.domain.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating an order.
 */
public record CreateOrderRequest(
    @NotNull(message = "Order type is required")
    OrderType type,

    UUID customerId,

    String customerName,

    String customerEmail,

    String customerPhone,

    LocalDate rentalStartDate,

    LocalDate rentalEndDate,

    BigDecimal depositAmount,

    BigDecimal taxAmount,

    BigDecimal discountAmount,

    String notes,

    @Valid
    List<CreateOrderItemRequest> items
) {}
