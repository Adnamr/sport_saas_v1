package com.sportsaas.order.domain;

import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.entity.OrderItem;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Order entity.
 */
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
            .tenantId(UUID.randomUUID())
            .orderNumber("ORD-20241217-0001")
            .type(OrderType.SALE)
            .status(OrderStatus.DRAFT)
            .paymentStatus(PaymentStatus.PENDING)
            .totalAmount(new BigDecimal("100.00"))
            .paidAmount(BigDecimal.ZERO)
            .build();
    }

    @Test
    void shouldCalculateRemainingAmount() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(new BigDecimal("30.00"));

        assertThat(order.getRemainingAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    void shouldReturnZeroRemainingWhenFullyPaid() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(new BigDecimal("100.00"));

        assertThat(order.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDetectFullyPaid() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(new BigDecimal("100.00"));

        assertThat(order.isFullyPaid()).isTrue();
    }

    @Test
    void shouldDetectNotFullyPaid() {
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setPaidAmount(new BigDecimal("50.00"));

        assertThat(order.isFullyPaid()).isFalse();
    }

    @Test
    void shouldDetectRentalOrder() {
        order.setType(OrderType.RENTAL);
        assertThat(order.isRental()).isTrue();

        order.setType(OrderType.SALE);
        assertThat(order.isRental()).isFalse();
    }

    @Test
    void shouldDetectOverdueRental() {
        order.setType(OrderType.RENTAL);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setRentalEndDate(LocalDate.now().minusDays(1));

        assertThat(order.isOverdue()).isTrue();
    }

    @Test
    void shouldNotBeOverdueWhenNotRental() {
        order.setType(OrderType.SALE);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setRentalEndDate(LocalDate.now().minusDays(1));

        assertThat(order.isOverdue()).isFalse();
    }

    @Test
    void shouldNotBeOverdueWhenFutureEndDate() {
        order.setType(OrderType.RENTAL);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setRentalEndDate(LocalDate.now().plusDays(5));

        assertThat(order.isOverdue()).isFalse();
    }

    @Test
    void shouldCanConfirmWhenDraft() {
        order.setStatus(OrderStatus.DRAFT);
        assertThat(order.canConfirm()).isTrue();
    }

    @Test
    void shouldCanConfirmWhenPending() {
        order.setStatus(OrderStatus.PENDING);
        assertThat(order.canConfirm()).isTrue();
    }

    @Test
    void shouldNotCanConfirmWhenConfirmed() {
        order.setStatus(OrderStatus.CONFIRMED);
        assertThat(order.canConfirm()).isFalse();
    }

    @Test
    void shouldCanCancelWhenDraft() {
        order.setStatus(OrderStatus.DRAFT);
        assertThat(order.canCancel()).isTrue();
    }

    @Test
    void shouldCanCancelWhenConfirmed() {
        order.setStatus(OrderStatus.CONFIRMED);
        assertThat(order.canCancel()).isTrue();
    }

    @Test
    void shouldNotCanCancelWhenCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        assertThat(order.canCancel()).isFalse();
    }

    @Test
    void shouldNotCanCancelWhenCancelled() {
        order.setStatus(OrderStatus.CANCELLED);
        assertThat(order.canCancel()).isFalse();
    }

    @Test
    void shouldCalculateRentalDuration() {
        order.setRentalStartDate(LocalDate.of(2024, 1, 1));
        order.setRentalEndDate(LocalDate.of(2024, 1, 8));

        assertThat(order.getRentalDurationDays()).isEqualTo(7);
    }

    @Test
    void shouldReturnNullDurationWhenDatesNotSet() {
        order.setRentalStartDate(null);
        order.setRentalEndDate(null);

        assertThat(order.getRentalDurationDays()).isNull();
    }

    @Test
    void shouldRecalculateTotals() {
        order.setTaxAmount(new BigDecimal("10.00"));
        order.setDiscountAmount(new BigDecimal("5.00"));

        OrderItem item1 = OrderItem.builder()
            .quantity(2)
            .unitPrice(new BigDecimal("25.00"))
            .totalPrice(new BigDecimal("50.00"))
            .build();

        OrderItem item2 = OrderItem.builder()
            .quantity(1)
            .unitPrice(new BigDecimal("30.00"))
            .totalPrice(new BigDecimal("30.00"))
            .build();

        order.addItem(item1);
        order.addItem(item2);

        // subtotal = 50 + 30 = 80
        // total = 80 + 10 (tax) - 5 (discount) = 85
        assertThat(order.getSubtotal()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("85.00"));
    }

    @Test
    void shouldHaveDefaultStatusDraft() {
        Order newOrder = Order.builder()
            .tenantId(UUID.randomUUID())
            .orderNumber("ORD-001")
            .build();

        assertThat(newOrder.getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    void shouldHaveDefaultTypeSale() {
        Order newOrder = Order.builder()
            .tenantId(UUID.randomUUID())
            .orderNumber("ORD-001")
            .build();

        assertThat(newOrder.getType()).isEqualTo(OrderType.SALE);
    }

    @Test
    void shouldHaveDefaultPaymentStatusPending() {
        Order newOrder = Order.builder()
            .tenantId(UUID.randomUUID())
            .orderNumber("ORD-001")
            .build();

        assertThat(newOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}
