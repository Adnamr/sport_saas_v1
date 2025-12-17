package com.sportsaas.inventory.infra.service;

import com.sportsaas.common.exception.ConflictException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.entity.Warehouse;
import com.sportsaas.inventory.infra.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for WarehouseServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse testWarehouse;
    private UUID warehouseId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        warehouseId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        testWarehouse = Warehouse.builder()
            .name("Main Warehouse")
            .address("123 Main St")
            .city("Paris")
            .active(true)
            .isDefault(false)
            .tenantId(tenantId)
            .build();
    }

    @Test
    void shouldCreateWarehouse() {
        when(warehouseRepository.existsByNameAndTenantId("Main Warehouse", tenantId)).thenReturn(false);
        when(warehouseRepository.countByTenantId(tenantId)).thenReturn(0L);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        Warehouse result = warehouseService.create(testWarehouse);

        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue(); // First warehouse becomes default
        verify(warehouseRepository).save(any(Warehouse.class));
    }

    @Test
    void shouldThrowExceptionWhenNameExists() {
        when(warehouseRepository.existsByNameAndTenantId("Main Warehouse", tenantId)).thenReturn(true);

        assertThatThrownBy(() -> warehouseService.create(testWarehouse))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateWarehouse() {
        Warehouse existing = Warehouse.builder()
            .name("Old Name")
            .tenantId(tenantId)
            .build();

        Warehouse updated = Warehouse.builder()
            .name("New Name")
            .address("456 New St")
            .city("Lyon")
            .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(existing));
        when(warehouseRepository.existsByNameAndTenantId("New Name", tenantId)).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(existing);

        Warehouse result = warehouseService.update(warehouseId, updated);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(warehouseRepository).save(existing);
    }

    @Test
    void shouldFindWarehouseById() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(testWarehouse));

        Optional<Warehouse> result = warehouseService.findById(warehouseId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Main Warehouse");
    }

    @Test
    void shouldFindWarehousesByTenantId() {
        Page<Warehouse> page = new PageImpl<>(List.of(testWarehouse));
        when(warehouseRepository.findByTenantId(tenantId, PageRequest.of(0, 10))).thenReturn(page);

        Page<Warehouse> result = warehouseService.findByTenantId(tenantId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindActiveWarehouses() {
        when(warehouseRepository.findByTenantIdAndActiveTrue(tenantId)).thenReturn(List.of(testWarehouse));

        List<Warehouse> result = warehouseService.findActiveByTenantId(tenantId);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldDeleteWarehouse() {
        testWarehouse.setIsDefault(false);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(testWarehouse));

        warehouseService.delete(warehouseId);

        verify(warehouseRepository).delete(testWarehouse);
    }

    @Test
    void shouldThrowExceptionWhenDeletingDefaultWarehouse() {
        testWarehouse.setIsDefault(true);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(testWarehouse));

        assertThatThrownBy(() -> warehouseService.delete(warehouseId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("default warehouse");
    }

    @Test
    void shouldActivateWarehouse() {
        testWarehouse.setActive(false);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        warehouseService.activate(warehouseId);

        assertThat(testWarehouse.getActive()).isTrue();
        verify(warehouseRepository).save(testWarehouse);
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingDefaultWarehouse() {
        testWarehouse.setIsDefault(true);
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(testWarehouse));

        assertThatThrownBy(() -> warehouseService.deactivate(warehouseId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("default warehouse");
    }

    @Test
    void shouldSetWarehouseAsDefault() {
        Warehouse existingDefault = Warehouse.builder()
            .name("Old Default")
            .isDefault(true)
            .tenantId(tenantId)
            .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.findByTenantIdAndIsDefaultTrue(tenantId)).thenReturn(Optional.of(existingDefault));
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(testWarehouse);

        warehouseService.setAsDefault(warehouseId);

        assertThat(testWarehouse.getIsDefault()).isTrue();
        assertThat(existingDefault.getIsDefault()).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenWarehouseDoesNotExist() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.activate(warehouseId))
            .isInstanceOf(NotFoundException.class);
    }
}
