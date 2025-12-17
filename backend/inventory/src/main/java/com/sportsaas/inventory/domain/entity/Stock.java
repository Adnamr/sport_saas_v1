package com.sportsaas.inventory.domain.entity;

import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Stock entity representing inventory level of a product in a warehouse.
 * Unique constraint on (tenant_id, product_id, warehouse_id).
 */
@Entity
@Table(name = "stocks", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stock_tenant_product_warehouse",
                     columnNames = {"tenant_id", "product_id", "warehouse_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Stock extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    @lombok.Builder.Default
    private Integer quantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @lombok.Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "alert_threshold")
    @lombok.Builder.Default
    private Integer alertThreshold = 5;

    /**
     * Returns available quantity (total - reserved).
     */
    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    /**
     * Checks if stock is below alert threshold.
     */
    public boolean isBelowThreshold() {
        return getAvailableQuantity() <= alertThreshold;
    }

    /**
     * Checks if requested quantity is available.
     */
    public boolean hasAvailable(int requestedQuantity) {
        return getAvailableQuantity() >= requestedQuantity;
    }
}
