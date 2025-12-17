package com.sportsaas.catalog.domain.entity;

import com.sportsaas.catalog.domain.enums.ProductCondition;
import com.sportsaas.catalog.domain.enums.ProductStatus;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity representing sports equipment items.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(length = 2000)
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_condition")
    @Builder.Default
    private ProductCondition condition = ProductCondition.NEW;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "rental_price_daily", precision = 10, scale = 2)
    private BigDecimal rentalPriceDaily;

    @Column(name = "rental_price_weekly", precision = 10, scale = 2)
    private BigDecimal rentalPriceWeekly;

    @Column(name = "rental_price_monthly", precision = 10, scale = 2)
    private BigDecimal rentalPriceMonthly;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    private String brand;

    private String model;

    @Column(name = "serial_number")
    private String serialNumber;

    private String size;

    private String color;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(length = 500)
    private String dimensions;

    @Column(name = "is_rentable")
    @Builder.Default
    private Boolean isRentable = true;

    @Column(name = "is_sellable")
    @Builder.Default
    private Boolean isSellable = false;

    @Column(name = "requires_deposit")
    @Builder.Default
    private Boolean requiresDeposit = true;

    @Column(name = "min_rental_days")
    @Builder.Default
    private Integer minRentalDays = 1;

    @Column(name = "max_rental_days")
    private Integer maxRentalDays;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "rental_count")
    @Builder.Default
    private Integer rentalCount = 0;

    /**
     * Checks if the product is available for rental.
     */
    public boolean isAvailableForRental() {
        return status == ProductStatus.ACTIVE && isRentable;
    }

    /**
     * Checks if the product is available for sale.
     */
    public boolean isAvailableForSale() {
        return status == ProductStatus.ACTIVE && isSellable;
    }

    /**
     * Gets the primary image URL.
     */
    public String getPrimaryImageUrl() {
        return images.stream()
            .filter(ProductImage::getIsPrimary)
            .findFirst()
            .map(ProductImage::getUrl)
            .orElse(images.isEmpty() ? null : images.get(0).getUrl());
    }

    /**
     * Adds an image to the product.
     */
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    /**
     * Removes an image from the product.
     */
    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    /**
     * Adds an attribute to the product.
     */
    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }

    /**
     * Removes an attribute from the product.
     */
    public void removeAttribute(ProductAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProduct(null);
    }
}
