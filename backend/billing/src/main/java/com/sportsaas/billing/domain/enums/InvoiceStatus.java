package com.sportsaas.billing.domain.enums;

/**
 * Invoice status enumeration.
 */
public enum InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    PARTIALLY_PAID,
    OVERDUE,
    CANCELLED,
    REFUNDED
}
