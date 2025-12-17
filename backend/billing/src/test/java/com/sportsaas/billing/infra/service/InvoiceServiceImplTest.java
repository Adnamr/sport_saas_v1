package com.sportsaas.billing.infra.service;

import com.sportsaas.billing.api.dto.CreateInvoiceItemRequest;
import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.InvoiceItem;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import com.sportsaas.billing.infra.repository.InvoiceItemRepository;
import com.sportsaas.billing.infra.repository.InvoiceRepository;
import com.sportsaas.billing.infra.repository.PaymentRepository;
import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.domain.service.OrderService;
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
import java.util.ArrayList;
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
 * Unit tests for InvoiceServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceItemRepository invoiceItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private UUID tenantId;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        invoice = Invoice.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .invoiceNumber("INV-20241217-0001")
            .status(InvoiceStatus.DRAFT)
            .customerId(UUID.randomUUID())
            .customerName("Test Customer")
            .customerEmail("test@example.com")
            .issueDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .subtotal(new BigDecimal("100.00"))
            .taxRate(new BigDecimal("20.00"))
            .taxAmount(new BigDecimal("20.00"))
            .totalAmount(new BigDecimal("120.00"))
            .paidAmount(BigDecimal.ZERO)
            .items(new ArrayList<>())
            .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreateInvoice() {
        Invoice newInvoice = Invoice.builder()
            .customerName("New Customer")
            .customerEmail("new@example.com")
            .build();

        List<CreateInvoiceItemRequest> items = List.of(
            new CreateInvoiceItemRequest(UUID.randomUUID(), "Item 1", 2, new BigDecimal("50.00"), BigDecimal.ZERO)
        );

        when(invoiceRepository.findLastInvoiceNumber(tenantId)).thenReturn(null);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        Invoice result = invoiceService.create(newInvoice, items);

        assertThat(result.getTenantId()).isEqualTo(tenantId);
        assertThat(result.getInvoiceNumber()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(result.getItems()).hasSize(1);
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void shouldFindInvoiceById() {
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.findById(invoice.getId());

        assertThat(result.getInvoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
    }

    @Test
    void shouldThrowWhenInvoiceNotFound() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findByIdAndTenantId(id, tenantId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.findById(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Invoice not found");
    }

    @Test
    void shouldFindByInvoiceNumber() {
        when(invoiceRepository.findByInvoiceNumberAndTenantId("INV-20241217-0001", tenantId))
            .thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.findByInvoiceNumber("INV-20241217-0001");

        assertThat(result.getId()).isEqualTo(invoice.getId());
    }

    @Test
    void shouldFindAll() {
        Page<Invoice> page = new PageImpl<>(List.of(invoice));
        when(invoiceRepository.findByTenantId(tenantId, PageRequest.of(0, 10)))
            .thenReturn(page);

        Page<Invoice> result = invoiceService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldSendInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.send(invoice.getId());

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(result.getSentAt()).isNotNull();
    }

    @Test
    void shouldNotSendAlreadySentInvoice() {
        invoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.send(invoice.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cannot be sent");
    }

    @Test
    void shouldMarkInvoiceAsPaid() {
        invoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.markAsPaid(invoice.getId());

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(result.getPaidAt()).isNotNull();
        assertThat(result.getPaidAmount()).isEqualTo(result.getTotalAmount());
    }

    @Test
    void shouldCancelInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.cancel(invoice.getId());

        assertThat(result.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
    }

    @Test
    void shouldNotCancelPaidInvoice() {
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.cancel(invoice.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cannot be cancelled");
    }

    @Test
    void shouldRecordPayment() {
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = invoiceService.recordPayment(invoice.getId(), new BigDecimal("50.00"),
            PaymentMethod.CARD, "TXN123", "Test payment");

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    void shouldMarkInvoiceAsPaidWhenFullyPaid() {
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        invoiceService.recordPayment(invoice.getId(), new BigDecimal("100.00"),
            PaymentMethod.CARD, "TXN123", null);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
    }

    @Test
    void shouldNotRecordZeroPayment() {
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.recordPayment(invoice.getId(), BigDecimal.ZERO,
            PaymentMethod.CARD, null, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("must be positive");
    }

    @Test
    void shouldDeleteDraftInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        invoiceService.delete(invoice.getId());

        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void shouldNotDeleteSentInvoice() {
        invoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.delete(invoice.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot delete invoice");

        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    void shouldGenerateInvoiceNumber() {
        when(invoiceRepository.findLastInvoiceNumber(tenantId)).thenReturn(null);

        String invoiceNumber = invoiceService.generateInvoiceNumber();

        assertThat(invoiceNumber).startsWith("INV-");
        assertThat(invoiceNumber).endsWith("-0001");
    }

    @Test
    void shouldIncrementInvoiceNumberSequence() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        when(invoiceRepository.findLastInvoiceNumber(tenantId))
            .thenReturn("INV-" + today + "-0005");

        String invoiceNumber = invoiceService.generateInvoiceNumber();

        assertThat(invoiceNumber).isEqualTo("INV-" + today + "-0006");
    }

    @Test
    void shouldGetStatistics() {
        when(invoiceRepository.countByTenantId(tenantId)).thenReturn(100L);
        when(invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.DRAFT)).thenReturn(10L);
        when(invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.SENT)).thenReturn(15L);
        when(invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.PAID)).thenReturn(50L);
        when(invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.PARTIALLY_PAID)).thenReturn(8L);
        when(invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.CANCELLED)).thenReturn(12L);
        when(invoiceRepository.countOverdueInvoices(tenantId)).thenReturn(5L);
        when(invoiceRepository.sumPaidInvoicesTotal(tenantId)).thenReturn(new BigDecimal("5000.00"));
        when(invoiceRepository.sumOutstandingAmount(tenantId)).thenReturn(new BigDecimal("2000.00"));

        Map<String, Object> stats = invoiceService.getStatistics();

        assertThat(stats.get("total")).isEqualTo(100L);
        assertThat(stats.get("paid")).isEqualTo(50L);
        assertThat(stats.get("overdue")).isEqualTo(5L);
    }

    @Test
    void shouldUpdateInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        Invoice updateData = Invoice.builder()
            .customerName("Updated Customer")
            .customerEmail("updated@example.com")
            .taxRate(new BigDecimal("15.00"))
            .build();

        List<CreateInvoiceItemRequest> items = List.of(
            new CreateInvoiceItemRequest(UUID.randomUUID(), "Updated Item", 3, new BigDecimal("30.00"), BigDecimal.ZERO)
        );

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.update(invoice.getId(), updateData, items);

        assertThat(result.getCustomerName()).isEqualTo("Updated Customer");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void shouldNotUpdateNonDraftInvoice() {
        invoice.setStatus(InvoiceStatus.SENT);
        Invoice updateData = Invoice.builder().customerName("Updated").build();

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.update(invoice.getId(), updateData, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot modify invoice");
    }

    @Test
    void shouldAddItemToInvoice() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        InvoiceItem item = InvoiceItem.builder()
            .description("New Item")
            .quantity(1)
            .unitPrice(new BigDecimal("25.00"))
            .discountPercent(BigDecimal.ZERO)
            .build();

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Invoice result = invoiceService.addItem(invoice.getId(), item);

        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void shouldNotAddItemToNonDraftInvoice() {
        invoice.setStatus(InvoiceStatus.SENT);
        InvoiceItem item = InvoiceItem.builder().description("Item").build();

        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> invoiceService.addItem(invoice.getId(), item))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot modify invoice");
    }
}
