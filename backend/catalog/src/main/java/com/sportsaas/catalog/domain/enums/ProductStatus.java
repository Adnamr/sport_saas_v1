package com.sportsaas.catalog.domain.enums;

/**
 * Possible statuses for a product.
 */
public enum ProductStatus {
    DRAFT,      // Product is being created
    ACTIVE,     // Product is available
    INACTIVE,   // Product is temporarily unavailable
    DISCONTINUED // Product is no longer available
}
