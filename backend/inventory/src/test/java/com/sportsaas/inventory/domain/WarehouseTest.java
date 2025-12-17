package com.sportsaas.inventory.domain;

import com.sportsaas.inventory.domain.entity.Warehouse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Warehouse entity.
 */
class WarehouseTest {

    @Test
    void shouldHaveDefaultActiveTrue() {
        Warehouse warehouse = Warehouse.builder()
            .name("Main Warehouse")
            .tenantId(UUID.randomUUID())
            .build();

        assertThat(warehouse.getActive()).isTrue();
    }

    @Test
    void shouldHaveDefaultIsDefaultFalse() {
        Warehouse warehouse = Warehouse.builder()
            .name("Secondary Warehouse")
            .tenantId(UUID.randomUUID())
            .build();

        assertThat(warehouse.getIsDefault()).isFalse();
    }

    @Test
    void shouldStoreAllFields() {
        UUID tenantId = UUID.randomUUID();
        Warehouse warehouse = Warehouse.builder()
            .name("Main Warehouse")
            .address("123 Main St")
            .city("Paris")
            .country("France")
            .postalCode("75001")
            .phone("+33123456789")
            .email("warehouse@example.com")
            .isDefault(true)
            .active(true)
            .notes("Main storage facility")
            .tenantId(tenantId)
            .build();

        assertThat(warehouse.getName()).isEqualTo("Main Warehouse");
        assertThat(warehouse.getAddress()).isEqualTo("123 Main St");
        assertThat(warehouse.getCity()).isEqualTo("Paris");
        assertThat(warehouse.getCountry()).isEqualTo("France");
        assertThat(warehouse.getPostalCode()).isEqualTo("75001");
        assertThat(warehouse.getPhone()).isEqualTo("+33123456789");
        assertThat(warehouse.getEmail()).isEqualTo("warehouse@example.com");
        assertThat(warehouse.getIsDefault()).isTrue();
        assertThat(warehouse.getActive()).isTrue();
        assertThat(warehouse.getNotes()).isEqualTo("Main storage facility");
        assertThat(warehouse.getTenantId()).isEqualTo(tenantId);
    }
}
