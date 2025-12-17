package com.sportsaas.order.infra.service;

import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.entity.OrderItem;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import com.sportsaas.order.infra.repository.OrderItemRepository;
import com.sportsaas.order.infra.repository.OrderRepository;
import com.sportsaas.tenant.domain.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OrderServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID tenantId;
    private Order order;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        order = Order.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .orderNumber("ORD-20241217-0001")
            .type(OrderType.SALE)
            .status(OrderStatus.DRAFT)
            .paymentStatus(PaymentStatus.PENDING)
            .subtotal(new BigDecimal("100.00"))
            .totalAmount(new BigDecimal("100.00"))
            .paidAmount(BigDecimal.ZERO)
            .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateOrder() {
        Order newOrder = Order.builder()
            .type(OrderType.SALE)
            .build();

        when(orderRepository.findLastOrderNumber(tenantId)).thenReturn(null);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        Order result = orderService.create(newOrder);

        assertThat(result.getTenantId()).isEqualTo(tenantId);
        assertThat(result.getOrderNumber()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.DRAFT);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldFindOrderById() {
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        Optional<Order> result = orderService.findById(order.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo(order.getOrderNumber());
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findByIdAndTenantId(id, tenantId))
            .thenReturn(Optional.empty());

        Optional<Order> result = orderService.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindByTenantId() {
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByTenantId(tenantId, PageRequest.of(0, 10)))
            .thenReturn(page);

        Page<Order> result = orderService.findByTenantId(tenantId, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldConfirmOrder() {
        order.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.confirm(order.getId());

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.getConfirmedAt()).isNotNull();
    }

    @Test
    void shouldNotConfirmAlreadyConfirmedOrder() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.confirm(order.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cannot be confirmed");
    }

    @Test
    void shouldCancelOrder() {
        order.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancel(order.getId(), "Customer request");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
        assertThat(result.getCancellationReason()).isEqualTo("Customer request");
    }

    @Test
    void shouldNotCancelCompletedOrder() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(order.getId(), "Reason"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void shouldRecordPayment() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(BigDecimal.ZERO);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.recordPayment(order.getId(), new BigDecimal("50.00"));

        assertThat(result.getPaidAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIAL);
    }

    @Test
    void shouldMarkAsPaidWhenFullyPaid() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(BigDecimal.ZERO);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.recordPayment(order.getId(), new BigDecimal("100.00"));

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void shouldReturnRental() {
        order.setType(OrderType.RENTAL);
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDate returnDate = LocalDate.now();
        Order result = orderService.returnRental(order.getId(), returnDate);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.RETURNED);
        assertThat(result.getActualReturnDate()).isEqualTo(returnDate);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldNotReturnNonRentalOrder() {
        order.setType(OrderType.SALE);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.returnRental(order.getId(), LocalDate.now()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("not a rental");
    }

    @Test
    void shouldDeleteDraftOrder() {
        order.setStatus(OrderStatus.DRAFT);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        orderService.delete(order.getId());

        verify(orderRepository).delete(order);
    }

    @Test
    void shouldNotDeleteConfirmedOrder() {
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.delete(order.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot delete order");

        verify(orderRepository, never()).delete(any());
    }

    @Test
    void shouldGenerateOrderNumber() {
        when(orderRepository.findLastOrderNumber(tenantId)).thenReturn(null);

        String orderNumber = orderService.generateOrderNumber(tenantId);

        assertThat(orderNumber).startsWith("ORD-");
        assertThat(orderNumber).endsWith("-0001");
    }

    @Test
    void shouldIncrementOrderNumberSequence() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        when(orderRepository.findLastOrderNumber(tenantId))
            .thenReturn("ORD-" + today + "-0005");

        String orderNumber = orderService.generateOrderNumber(tenantId);

        assertThat(orderNumber).isEqualTo("ORD-" + today + "-0006");
    }

    @Test
    void shouldGetStatistics() {
        when(orderRepository.countByTenantId(tenantId)).thenReturn(100L);
        when(orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.DRAFT)).thenReturn(10L);
        when(orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.PENDING)).thenReturn(5L);
        when(orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.CONFIRMED)).thenReturn(15L);
        when(orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.IN_PROGRESS)).thenReturn(8L);
        when(orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.COMPLETED)).thenReturn(50L);
        when(orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.CANCELLED)).thenReturn(12L);
        when(orderRepository.countByTenantIdAndType(tenantId, OrderType.SALE)).thenReturn(70L);
        when(orderRepository.countByTenantIdAndType(tenantId, OrderType.RENTAL)).thenReturn(30L);
        when(orderRepository.countOverdueRentals(tenantId)).thenReturn(3L);
        when(orderRepository.sumCompletedOrdersTotal(tenantId)).thenReturn(new BigDecimal("5000.00"));

        Map<String, Object> stats = orderService.getStatistics(tenantId);

        assertThat(stats.get("total")).isEqualTo(100L);
        assertThat(stats.get("completed")).isEqualTo(50L);
        assertThat(stats.get("sales")).isEqualTo(70L);
        assertThat(stats.get("rentals")).isEqualTo(30L);
        assertThat(stats.get("overdueRentals")).isEqualTo(3L);
    }

    @Test
    void shouldRefundPayment() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(new BigDecimal("100.00"));
        order.setPaymentStatus(PaymentStatus.PAID);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.refund(order.getId(), new BigDecimal("50.00"));

        assertThat(result.getPaidAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PARTIAL);
    }

    @Test
    void shouldNotRefundMoreThanPaid() {
        order.setPaidAmount(new BigDecimal("50.00"));
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.refund(order.getId(), new BigDecimal("100.00")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("exceeds paid amount");
    }

    @Test
    void shouldReturnDeposit() {
        order.setType(OrderType.RENTAL);
        order.setDepositAmount(new BigDecimal("200.00"));
        order.setDepositReturned(false);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.returnDeposit(order.getId());

        assertThat(result.getDepositReturned()).isTrue();
    }

    @Test
    void shouldNotReturnDepositForNonRental() {
        order.setType(OrderType.SALE);
        when(orderRepository.findByIdAndTenantId(order.getId(), tenantId))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.returnDeposit(order.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("not a rental");
    }
}
