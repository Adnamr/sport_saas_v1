package com.sportsaas.billing.domain.entity;

import com.sportsaas.billing.domain.enums.PaymentMethod;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity for recording payments.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Payment extends TenantAwareEntity {

    @Column(name = "payment_number", nullable = false, unique = true)
    private String paymentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private PaymentMethod method = PaymentMethod.CASH;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(length = 500)
    private String notes;

    @Column(name = "is_refund")
    @lombok.Builder.Default
    private Boolean isRefund = false;

    @Column(name = "refund_of_payment_id")
    private UUID refundOfPaymentId;

    /**
     * Check if this is a refund payment.
     */
    public boolean isRefundPayment() {
        return Boolean.TRUE.equals(isRefund);
    }
}
