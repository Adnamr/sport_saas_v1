package com.sportsaas.order.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for updating an order.
 */
public record UpdateOrderRequest(
    UUID customerId,

    String customerName,

    String customerEmail,

    String customerPhone,

    LocalDate rentalStartDate,

    LocalDate rentalEndDate,

    BigDecimal depositAmount,

    BigDecimal taxAmount,

    BigDecimal discountAmount,

    String notes
) {}
