package com.sportsaas.billing.api.dto;

import com.sportsaas.billing.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Payment.
 */
public record PaymentResponse(
    UUID id,
    String paymentNumber,
    UUID invoiceId,
    UUID orderId,
    BigDecimal amount,
    PaymentMethod method,
    LocalDateTime paymentDate,
    String transactionReference,
    String notes,
    Boolean isRefund,
    UUID refundOfPaymentId,
    LocalDateTime createdAt
) {}
