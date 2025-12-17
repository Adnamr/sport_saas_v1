package com.sportsaas.order.domain;

import com.sportsaas.order.domain.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderItem entity.
 */
class OrderItemTest {

    private OrderItem item;

    @BeforeEach
    void setUp() {
        item = OrderItem.builder()
            .tenantId(UUID.randomUUID())
            .productName("Test Product")
            .quantity(2)
            .unitPrice(new BigDecimal("50.00"))
            .discountPercent(BigDecimal.ZERO)
            .build();
    }

    @Test
    void shouldCalculateTotalPriceWithoutDiscount() {
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("25.00"));
        item.setDiscountPercent(BigDecimal.ZERO);

        item.calculateTotalPrice();

        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    void shouldCalculateTotalPriceWithDiscount() {
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setDiscountPercent(new BigDecimal("10.00")); // 10% discount

        item.calculateTotalPrice();

        // 2 * 100 = 200, with 10% discount = 180
        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("180.00"));
    }

    @Test
    void shouldCalculateTotalPriceWithHighDiscount() {
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("200.00"));
        item.setDiscountPercent(new BigDecimal("25.00")); // 25% discount

        item.calculateTotalPrice();

        // 200 with 25% discount = 150
        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldGetEffectiveUnitPriceWithoutDiscount() {
        item.setUnitPrice(new BigDecimal("50.00"));
        item.setDiscountPercent(BigDecimal.ZERO);

        assertThat(item.getEffectiveUnitPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldGetEffectiveUnitPriceWithDiscount() {
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setDiscountPercent(new BigDecimal("20.00")); // 20% discount

        // 100 with 20% discount = 80
        assertThat(item.getEffectiveUnitPrice()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void shouldHandleNullDiscountPercent() {
        item.setUnitPrice(new BigDecimal("50.00"));
        item.setDiscountPercent(null);

        assertThat(item.getEffectiveUnitPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void shouldCalculateCorrectlyForSingleItem() {
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("99.99"));
        item.setDiscountPercent(BigDecimal.ZERO);

        item.calculateTotalPrice();

        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void shouldCalculateCorrectlyForLargeQuantity() {
        item.setQuantity(100);
        item.setUnitPrice(new BigDecimal("9.99"));
        item.setDiscountPercent(BigDecimal.ZERO);

        item.calculateTotalPrice();

        assertThat(item.getTotalPrice()).isEqualByComparingTo(new BigDecimal("999.00"));
    }

    @Test
    void shouldHaveDefaultDiscountPercentZero() {
        OrderItem newItem = OrderItem.builder()
            .tenantId(UUID.randomUUID())
            .productName("Test")
            .quantity(1)
            .unitPrice(new BigDecimal("10.00"))
            .build();

        assertThat(newItem.getDiscountPercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
