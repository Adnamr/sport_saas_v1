package com.sportsaas.inventory.domain.enums;

/**
 * Status of a stock reservation.
 */
public enum ReservationStatus {
    PENDING,    // Reservation created, awaiting confirmation
    CONFIRMED,  // Reservation confirmed, stock decremented
    RELEASED,   // Reservation released/cancelled
    EXPIRED     // Reservation expired automatically
}
