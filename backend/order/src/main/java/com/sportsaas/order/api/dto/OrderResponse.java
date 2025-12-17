package com.sportsaas.order.api.dto;

import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Order.
 */
public record OrderResponse(
    UUID id,
    String orderNumber,
    OrderType type,
    OrderStatus status,
    PaymentStatus paymentStatus,
    UUID customerId,
    String customerName,
    String customerEmail,
    String customerPhone,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal remainingAmount,
    LocalDate rentalStartDate,
    LocalDate rentalEndDate,
    LocalDate actualReturnDate,
    Integer rentalDurationDays,
    BigDecimal depositAmount,
    Boolean depositReturned,
    Boolean isOverdue,
    String notes,
    LocalDateTime confirmedAt,
    LocalDateTime completedAt,
    LocalDateTime cancelledAt,
    String cancellationReason,
    List<OrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
