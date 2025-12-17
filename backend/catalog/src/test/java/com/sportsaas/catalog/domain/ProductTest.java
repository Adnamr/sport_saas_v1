package com.sportsaas.catalog.domain;

import com.sportsaas.catalog.domain.entity.Category;
import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.entity.ProductImage;
import com.sportsaas.catalog.domain.enums.ProductCondition;
import com.sportsaas.catalog.domain.enums.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Product entity.
 */
class ProductTest {

    private Product product;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .name("Bikes")
            .slug("bikes")
            .tenantId(UUID.randomUUID())
            .build();

        product = Product.builder()
            .name("Mountain Bike Pro")
            .slug("mountain-bike-pro")
            .sku("MTB-PRO-001")
            .description("Professional mountain bike")
            .shortDescription("Pro MTB")
            .category(category)
            .status(ProductStatus.DRAFT)
            .condition(ProductCondition.NEW)
            .rentalPriceDaily(new BigDecimal("50.00"))
            .rentalPriceWeekly(new BigDecimal("280.00"))
            .rentalPriceMonthly(new BigDecimal("900.00"))
            .depositAmount(new BigDecimal("500.00"))
            .brand("Trek")
            .model("X-Caliber 8")
            .isRentable(true)
            .isSellable(false)
            .requiresDeposit(true)
            .minRentalDays(1)
            .viewCount(0)
            .rentalCount(0)
            .tenantId(UUID.randomUUID())
            .images(new ArrayList<>())
            .attributes(new ArrayList<>())
            .build();
    }

    @Test
    void shouldHaveDefaultStatusDraft() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void shouldHaveDefaultConditionNew() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getCondition()).isEqualTo(ProductCondition.NEW);
    }

    @Test
    void shouldHaveDefaultIsRentableTrue() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getIsRentable()).isTrue();
    }

    @Test
    void shouldHaveDefaultIsSellableFalse() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getIsSellable()).isFalse();
    }

    @Test
    void shouldHaveDefaultRequiresDepositTrue() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getRequiresDeposit()).isTrue();
    }

    @Test
    void shouldHaveDefaultMinRentalDaysOne() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getMinRentalDays()).isEqualTo(1);
    }

    @Test
    void shouldHaveDefaultViewCountZero() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getViewCount()).isZero();
    }

    @Test
    void shouldHaveDefaultRentalCountZero() {
        // Given
        Product newProduct = Product.builder()
            .name("New Product")
            .slug("new-product")
            .sku("NEW-001")
            .tenantId(UUID.randomUUID())
            .build();

        // Then
        assertThat(newProduct.getRentalCount()).isZero();
    }

    @Test
    void shouldReturnNullWhenNoImages() {
        // Given
        product.setImages(new ArrayList<>());

        // When
        String primaryImageUrl = product.getPrimaryImageUrl();

        // Then
        assertThat(primaryImageUrl).isNull();
    }

    @Test
    void shouldReturnPrimaryImageUrl() {
        // Given
        ProductImage primaryImage = ProductImage.builder()
            .url("https://example.com/primary.jpg")
            .isPrimary(true)
            .tenantId(product.getTenantId())
            .build();

        ProductImage secondaryImage = ProductImage.builder()
            .url("https://example.com/secondary.jpg")
            .isPrimary(false)
            .tenantId(product.getTenantId())
            .build();

        product.setImages(List.of(secondaryImage, primaryImage));

        // When
        String primaryImageUrl = product.getPrimaryImageUrl();

        // Then
        assertThat(primaryImageUrl).isEqualTo("https://example.com/primary.jpg");
    }

    @Test
    void shouldReturnFirstImageUrlWhenNoPrimary() {
        // Given
        ProductImage firstImage = ProductImage.builder()
            .url("https://example.com/first.jpg")
            .isPrimary(false)
            .tenantId(product.getTenantId())
            .build();

        ProductImage secondImage = ProductImage.builder()
            .url("https://example.com/second.jpg")
            .isPrimary(false)
            .tenantId(product.getTenantId())
            .build();

        product.setImages(List.of(firstImage, secondImage));

        // When
        String primaryImageUrl = product.getPrimaryImageUrl();

        // Then
        assertThat(primaryImageUrl).isEqualTo("https://example.com/first.jpg");
    }

    @Test
    void shouldCalculateCorrectRentalPrices() {
        // Then
        assertThat(product.getRentalPriceDaily()).isEqualTo(new BigDecimal("50.00"));
        assertThat(product.getRentalPriceWeekly()).isEqualTo(new BigDecimal("280.00"));
        assertThat(product.getRentalPriceMonthly()).isEqualTo(new BigDecimal("900.00"));
    }

    @Test
    void shouldHaveCorrectCategory() {
        // Then
        assertThat(product.getCategory()).isNotNull();
        assertThat(product.getCategory().getName()).isEqualTo("Bikes");
    }
}
