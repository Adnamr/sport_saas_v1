package com.sportsaas.tenant.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for TenantContext.
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndGetTenantId() {
        // Given
        UUID tenantId = UUID.randomUUID();

        // When
        TenantContext.setTenantId(tenantId);

        // Then
        assertThat(TenantContext.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void shouldReturnNullWhenNoTenantSet() {
        // When/Then
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void shouldClearTenantId() {
        // Given
        TenantContext.setTenantId(UUID.randomUUID());

        // When
        TenantContext.clear();

        // Then
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    void shouldReturnTrueWhenHasTenantId() {
        // Given
        TenantContext.setTenantId(UUID.randomUUID());

        // When/Then
        assertThat(TenantContext.hasTenantId()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoTenantId() {
        // When/Then
        assertThat(TenantContext.hasTenantId()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenRequireTenantIdAndNoTenantSet() {
        // When/Then
        assertThatThrownBy(TenantContext::requireTenantId)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No tenant ID set");
    }

    @Test
    void shouldReturnTenantIdWhenRequireTenantIdAndTenantSet() {
        // Given
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        // When
        UUID result = TenantContext.requireTenantId();

        // Then
        assertThat(result).isEqualTo(tenantId);
    }
}
