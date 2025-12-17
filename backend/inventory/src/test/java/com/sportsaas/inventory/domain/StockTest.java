package com.sportsaas.inventory.domain;

import com.sportsaas.inventory.domain.entity.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Stock entity.
 */
class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = Stock.builder()
            .tenantId(UUID.randomUUID())
            .quantity(100)
            .reservedQuantity(20)
            .alertThreshold(10)
            .build();
    }

    @Test
    void shouldCalculateAvailableQuantity() {
        assertThat(stock.getAvailableQuantity()).isEqualTo(80);
    }

    @Test
    void shouldReturnZeroAvailableWhenAllReserved() {
        stock.setQuantity(50);
        stock.setReservedQuantity(50);

        assertThat(stock.getAvailableQuantity()).isZero();
    }

    @Test
    void shouldDetectBelowThreshold() {
        stock.setQuantity(15);
        stock.setReservedQuantity(10);
        // Available = 5, threshold = 10

        assertThat(stock.isBelowThreshold()).isTrue();
    }

    @Test
    void shouldNotBeBelowThresholdWhenAbove() {
        stock.setQuantity(100);
        stock.setReservedQuantity(0);
        // Available = 100, threshold = 10

        assertThat(stock.isBelowThreshold()).isFalse();
    }

    @Test
    void shouldCheckAvailabilityPositive() {
        assertThat(stock.hasAvailable(50)).isTrue();
        assertThat(stock.hasAvailable(80)).isTrue();
    }

    @Test
    void shouldCheckAvailabilityNegative() {
        assertThat(stock.hasAvailable(81)).isFalse();
        assertThat(stock.hasAvailable(100)).isFalse();
    }

    @Test
    void shouldHaveDefaultQuantityZero() {
        Stock newStock = Stock.builder()
            .tenantId(UUID.randomUUID())
            .build();

        assertThat(newStock.getQuantity()).isZero();
    }

    @Test
    void shouldHaveDefaultReservedQuantityZero() {
        Stock newStock = Stock.builder()
            .tenantId(UUID.randomUUID())
            .build();

        assertThat(newStock.getReservedQuantity()).isZero();
    }

    @Test
    void shouldHaveDefaultAlertThresholdFive() {
        Stock newStock = Stock.builder()
            .tenantId(UUID.randomUUID())
            .build();

        assertThat(newStock.getAlertThreshold()).isEqualTo(5);
    }
}
