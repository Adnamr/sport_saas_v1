package com.sportsaas.inventory.infra.service;

import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.entity.Stock;
import com.sportsaas.inventory.domain.entity.StockMovement;
import com.sportsaas.inventory.domain.enums.MovementType;
import com.sportsaas.inventory.infra.repository.StockMovementRepository;
import com.sportsaas.inventory.infra.repository.StockRepository;
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
 * Unit tests for StockServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock testStock;
    private UUID stockId;
    private UUID tenantId;
    private UUID productId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        stockId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        testStock = Stock.builder()
            .quantity(100)
            .reservedQuantity(20)
            .alertThreshold(10)
            .tenantId(tenantId)
            .build();
    }

    @Test
    void shouldCreateStock() {
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);

        Stock result = stockService.create(testStock);

        assertThat(result).isNotNull();
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void shouldFindStockById() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));

        Optional<Stock> result = stockService.findById(stockId);

        assertThat(result).isPresent();
    }

    @Test
    void shouldFindStockByTenantId() {
        Page<Stock> page = new PageImpl<>(List.of(testStock));
        when(stockRepository.findByTenantId(tenantId, PageRequest.of(0, 10))).thenReturn(page);

        Page<Stock> result = stockService.findByTenantId(tenantId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldFindLowStock() {
        Page<Stock> page = new PageImpl<>(List.of(testStock));
        when(stockRepository.findLowStockByTenantId(tenantId, PageRequest.of(0, 10))).thenReturn(page);

        Page<Stock> result = stockService.findLowStock(tenantId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldAddStock() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        StockMovement result = stockService.addStock(stockId, 50, "Purchase", "PO-001", null);

        assertThat(testStock.getQuantity()).isEqualTo(150);
        assertThat(result.getType()).isEqualTo(MovementType.IN);
        assertThat(result.getQuantity()).isEqualTo(50);
        assertThat(result.getQuantityBefore()).isEqualTo(100);
        assertThat(result.getQuantityAfter()).isEqualTo(150);
    }

    @Test
    void shouldThrowExceptionWhenAddingNegativeQuantity() {
        assertThatThrownBy(() -> stockService.addStock(stockId, -10, "Error", null, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("positive");
    }

    @Test
    void shouldRemoveStock() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        StockMovement result = stockService.removeStock(stockId, 30, "Sale", "SO-001", null);

        assertThat(testStock.getQuantity()).isEqualTo(70);
        assertThat(result.getType()).isEqualTo(MovementType.OUT);
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));

        // Available = 100 - 20 = 80, trying to remove 90
        assertThatThrownBy(() -> stockService.removeStock(stockId, 90, "Error", null, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Insufficient");
    }

    @Test
    void shouldAdjustStock() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        StockMovement result = stockService.adjustStock(stockId, 75, "Inventory count", null);

        assertThat(testStock.getQuantity()).isEqualTo(75);
        assertThat(result.getType()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(result.getQuantityBefore()).isEqualTo(100);
        assertThat(result.getQuantityAfter()).isEqualTo(75);
    }

    @Test
    void shouldReserveStock() {
        UUID orderId = UUID.randomUUID();
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        StockMovement result = stockService.reserveStock(stockId, 30, orderId, null);

        assertThat(testStock.getReservedQuantity()).isEqualTo(50);
        assertThat(result.getType()).isEqualTo(MovementType.RESERVATION);
        assertThat(result.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void shouldReleaseStock() {
        UUID orderId = UUID.randomUUID();
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        StockMovement result = stockService.releaseStock(stockId, 10, orderId, null);

        assertThat(testStock.getReservedQuantity()).isEqualTo(10);
        assertThat(result.getType()).isEqualTo(MovementType.RELEASE);
    }

    @Test
    void shouldThrowExceptionWhenReleasingMoreThanReserved() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));

        assertThatThrownBy(() -> stockService.releaseStock(stockId, 30, null, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("release more than reserved");
    }

    @Test
    void shouldGetAvailableQuantity() {
        when(stockRepository.getAvailableQuantityByProduct(tenantId, productId)).thenReturn(80);

        Integer result = stockService.getAvailableQuantity(tenantId, productId);

        assertThat(result).isEqualTo(80);
    }

    @Test
    void shouldCheckAvailability() {
        when(stockRepository.getAvailableQuantityByProduct(tenantId, productId)).thenReturn(80);

        assertThat(stockService.checkAvailability(tenantId, productId, 50)).isTrue();
        assertThat(stockService.checkAvailability(tenantId, productId, 100)).isFalse();
    }

    @Test
    void shouldThrowNotFoundWhenStockDoesNotExist() {
        when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.addStock(stockId, 10, "Test", null, null))
            .isInstanceOf(NotFoundException.class);
    }
}
