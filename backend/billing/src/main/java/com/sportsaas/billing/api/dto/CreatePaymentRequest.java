package com.sportsaas.billing.api.dto;

import com.sportsaas.billing.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a payment.
 */
public record CreatePaymentRequest(
    UUID invoiceId,

    UUID orderId,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Payment method is required")
    PaymentMethod method,

    String transactionReference,

    String notes
) {}
