package com.sportsaas.inventory.domain.enums;

/**
 * Types of stock movements.
 */
public enum MovementType {
    IN,          // Stock entry (purchase, return)
    OUT,         // Stock exit (sale, rental)
    ADJUSTMENT,  // Manual inventory adjustment
    RESERVATION, // Reserve stock for order
    RELEASE      // Release reserved stock
}
