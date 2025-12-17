package com.sportsaas.order.domain.entity;

import com.sportsaas.common.domain.TenantAwareEntity;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order entity representing sales or rentals.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends TenantAwareEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private OrderType type = OrderType.SALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @lombok.Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "subtotal", precision = 10, scale = 2)
    @lombok.Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @lombok.Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @lombok.Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @lombok.Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    @lombok.Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "rental_start_date")
    private LocalDate rentalStartDate;

    @Column(name = "rental_end_date")
    private LocalDate rentalEndDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "deposit_returned")
    @lombok.Builder.Default
    private Boolean depositReturned = false;

    @Column(length = 1000)
    private String notes;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Add an item to the order.
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotals();
    }

    /**
     * Remove an item from the order.
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotals();
    }

    /**
     * Recalculate order totals based on items.
     */
    public void recalculateTotals() {
        this.subtotal = items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = this.subtotal
            .add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO)
            .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }

    /**
     * Get remaining amount to pay.
     */
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount != null ? paidAmount : BigDecimal.ZERO);
    }

    /**
     * Check if order is fully paid.
     */
    public boolean isFullyPaid() {
        return paidAmount != null && paidAmount.compareTo(totalAmount) >= 0;
    }

    /**
     * Check if order is a rental.
     */
    public boolean isRental() {
        return type == OrderType.RENTAL;
    }

    /**
     * Check if rental is overdue.
     */
    public boolean isOverdue() {
        if (!isRental() || rentalEndDate == null) {
            return false;
        }
        return status == OrderStatus.IN_PROGRESS && LocalDate.now().isAfter(rentalEndDate);
    }

    /**
     * Check if order can be confirmed.
     */
    public boolean canConfirm() {
        return status == OrderStatus.DRAFT || status == OrderStatus.PENDING;
    }

    /**
     * Check if order can be cancelled.
     */
    public boolean canCancel() {
        return status != OrderStatus.COMPLETED &&
               status != OrderStatus.CANCELLED &&
               status != OrderStatus.RETURNED;
    }

    /**
     * Get rental duration in days.
     */
    public Integer getRentalDurationDays() {
        if (rentalStartDate == null || rentalEndDate == null) {
            return null;
        }
        return (int) (rentalEndDate.toEpochDay() - rentalStartDate.toEpochDay());
    }
}
