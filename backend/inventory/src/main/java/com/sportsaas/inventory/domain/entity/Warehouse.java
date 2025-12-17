package com.sportsaas.inventory.domain.entity;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Warehouse entity for storing stock locations.
 * Each tenant can have multiple warehouses with one default.
 */
@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Warehouse extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "is_default")
    @lombok.Builder.Default
    private Boolean isDefault = false;

    @Column(nullable = false)
    @lombok.Builder.Default
    private Boolean active = true;

    @Column(length = 1000)
    private String notes;
}
