package com.sportsaas.inventory.domain.entity;

import com.sportsaas.common.domain.TenantAwareEntity;
import com.sportsaas.inventory.domain.enums.ReservationStatus;
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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reservation entity for reserving stock for orders.
 * Reservations can expire automatically if not confirmed.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Reservation extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(length = 500)
    private String notes;

    /**
     * Checks if reservation is expired.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt)
               && status == ReservationStatus.PENDING;
    }

    /**
     * Checks if reservation can be confirmed.
     */
    public boolean canConfirm() {
        return status == ReservationStatus.PENDING && !isExpired();
    }

    /**
     * Checks if reservation can be released.
     */
    public boolean canRelease() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }
}
