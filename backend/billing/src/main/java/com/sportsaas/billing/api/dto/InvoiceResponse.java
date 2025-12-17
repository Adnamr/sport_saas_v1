package com.sportsaas.billing.api.dto;

import com.sportsaas.billing.domain.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Invoice.
 */
public record InvoiceResponse(
    UUID id,
    String invoiceNumber,
    UUID orderId,
    InvoiceStatus status,
    UUID customerId,
    String customerName,
    String customerEmail,
    String customerAddress,
    String customerTaxId,
    LocalDate issueDate,
    LocalDate dueDate,
    BigDecimal subtotal,
    BigDecimal taxRate,
    BigDecimal taxAmount,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal remainingAmount,
    Boolean isOverdue,
    Integer daysUntilDue,
    String notes,
    LocalDateTime sentAt,
    LocalDateTime paidAt,
    LocalDateTime cancelledAt,
    List<InvoiceItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
