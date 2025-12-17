package com.sportsaas.billing.domain;

import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.InvoiceItem;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Invoice entity.
 */
class InvoiceTest {

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .invoiceNumber("INV-001")
            .status(InvoiceStatus.DRAFT)
            .issueDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .subtotal(BigDecimal.ZERO)
            .taxRate(new BigDecimal("20.00"))
            .taxAmount(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .totalAmount(BigDecimal.ZERO)
            .paidAmount(BigDecimal.ZERO)
            .items(new ArrayList<>())
            .build();
    }

    @Test
    void shouldAddItem() {
        InvoiceItem item = InvoiceItem.builder()
            .description("Test Item")
            .quantity(2)
            .unitPrice(new BigDecimal("50.00"))
            .discountPercent(BigDecimal.ZERO)
            .totalPrice(new BigDecimal("100.00"))
            .build();

        invoice.addItem(item);

        assertThat(invoice.getItems()).hasSize(1);
        assertThat(item.getInvoice()).isEqualTo(invoice);
    }

    @Test
    void shouldRemoveItem() {
        InvoiceItem item = InvoiceItem.builder()
            .description("Test Item")
            .quantity(1)
            .unitPrice(new BigDecimal("50.00"))
            .discountPercent(BigDecimal.ZERO)
            .totalPrice(new BigDecimal("50.00"))
            .build();

        invoice.addItem(item);
        invoice.removeItem(item);

        assertThat(invoice.getItems()).isEmpty();
        assertThat(item.getInvoice()).isNull();
    }

    @Test
    void shouldRecalculateTotals() {
        InvoiceItem item1 = InvoiceItem.builder()
            .description("Item 1")
            .quantity(2)
            .unitPrice(new BigDecimal("50.00"))
            .discountPercent(BigDecimal.ZERO)
            .totalPrice(new BigDecimal("100.00"))
            .build();

        InvoiceItem item2 = InvoiceItem.builder()
            .description("Item 2")
            .quantity(1)
            .unitPrice(new BigDecimal("30.00"))
            .discountPercent(BigDecimal.ZERO)
            .totalPrice(new BigDecimal("30.00"))
            .build();

        invoice.addItem(item1);
        invoice.addItem(item2);
        invoice.recalculateTotals();

        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("130.00"));
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo(new BigDecimal("26.00")); // 20% tax
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("156.00"));
    }

    @Test
    void shouldRecalculateTotalsWithDiscount() {
        invoice.setDiscountAmount(new BigDecimal("10.00"));

        InvoiceItem item = InvoiceItem.builder()
            .description("Item")
            .quantity(1)
            .unitPrice(new BigDecimal("100.00"))
            .discountPercent(BigDecimal.ZERO)
            .totalPrice(new BigDecimal("100.00"))
            .build();

        invoice.addItem(item);
        invoice.recalculateTotals();

        // Subtotal: 100, Tax: 20 (20%), Discount: 10, Total: 110
        assertThat(invoice.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("110.00"));
    }

    @Test
    void shouldCalculateRemainingAmount() {
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(new BigDecimal("40.00"));

        BigDecimal remaining = invoice.getRemainingAmount();

        assertThat(remaining).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void shouldBeFullyPaid() {
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(new BigDecimal("100.00"));

        assertThat(invoice.isFullyPaid()).isTrue();
    }

    @Test
    void shouldNotBeFullyPaidWhenPartiallyPaid() {
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(new BigDecimal("50.00"));

        assertThat(invoice.isFullyPaid()).isFalse();
    }

    @Test
    void shouldBeOverdueWhenPastDueDate() {
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setDueDate(LocalDate.now().minusDays(1));

        assertThat(invoice.isOverdue()).isTrue();
    }

    @Test
    void shouldNotBeOverdueWhenPaid() {
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setDueDate(LocalDate.now().minusDays(1));

        assertThat(invoice.isOverdue()).isFalse();
    }

    @Test
    void shouldNotBeOverdueWhenNotYetDue() {
        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setDueDate(LocalDate.now().plusDays(10));

        assertThat(invoice.isOverdue()).isFalse();
    }

    @Test
    void shouldBeAbleToSendDraftInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);

        assertThat(invoice.canSend()).isTrue();
    }

    @Test
    void shouldNotBeAbleToSendSentInvoice() {
        invoice.setStatus(InvoiceStatus.SENT);

        assertThat(invoice.canSend()).isFalse();
    }

    @Test
    void shouldBeAbleToCancelDraftInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);

        assertThat(invoice.canCancel()).isTrue();
    }

    @Test
    void shouldBeAbleToCancelSentInvoice() {
        invoice.setStatus(InvoiceStatus.SENT);

        assertThat(invoice.canCancel()).isTrue();
    }

    @Test
    void shouldNotBeAbleToCancelPaidInvoice() {
        invoice.setStatus(InvoiceStatus.PAID);

        assertThat(invoice.canCancel()).isFalse();
    }
}
