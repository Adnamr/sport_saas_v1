package com.sportsaas.billing.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating an invoice.
 */
public record CreateInvoiceRequest(
    UUID orderId,

    UUID customerId,

    String customerName,

    String customerEmail,

    String customerAddress,

    String customerTaxId,

    LocalDate issueDate,

    @NotNull(message = "Due date is required")
    LocalDate dueDate,

    BigDecimal taxRate,

    BigDecimal discountAmount,

    String notes,

    @Valid
    List<CreateInvoiceItemRequest> items
) {}
