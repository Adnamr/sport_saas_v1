package com.sportsaas.billing.infra.service;

import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import com.sportsaas.billing.infra.repository.InvoiceRepository;
import com.sportsaas.billing.infra.repository.PaymentRepository;
import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.domain.entity.Order;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PaymentServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID tenantId;
    private Payment payment;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        invoice = Invoice.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .invoiceNumber("INV-20241217-0001")
            .status(InvoiceStatus.SENT)
            .totalAmount(new BigDecimal("100.00"))
            .paidAmount(BigDecimal.ZERO)
            .build();

        payment = Payment.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .paymentNumber("PAY-20241217-0001")
            .invoice(invoice)
            .amount(new BigDecimal("50.00"))
            .method(PaymentMethod.CARD)
            .paymentDate(LocalDateTime.now())
            .isRefund(false)
            .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldCreatePayment() {
        Payment newPayment = Payment.builder()
            .amount(new BigDecimal("100.00"))
            .method(PaymentMethod.CASH)
            .build();

        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        Payment result = paymentService.create(newPayment);

        assertThat(result.getTenantId()).isEqualTo(tenantId);
        assertThat(result.getPaymentNumber()).isNotNull();
        assertThat(result.getPaymentDate()).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void shouldFindPaymentById() {
        when(paymentRepository.findByIdAndTenantId(payment.getId(), tenantId))
            .thenReturn(Optional.of(payment));

        Payment result = paymentService.findById(payment.getId());

        assertThat(result.getPaymentNumber()).isEqualTo(payment.getPaymentNumber());
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        UUID id = UUID.randomUUID();
        when(paymentRepository.findByIdAndTenantId(id, tenantId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.findById(id))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Payment not found");
    }

    @Test
    void shouldFindByPaymentNumber() {
        when(paymentRepository.findByPaymentNumberAndTenantId("PAY-20241217-0001", tenantId))
            .thenReturn(Optional.of(payment));

        Payment result = paymentService.findByPaymentNumber("PAY-20241217-0001");

        assertThat(result.getId()).isEqualTo(payment.getId());
    }

    @Test
    void shouldFindAll() {
        Page<Payment> page = new PageImpl<>(List.of(payment));
        when(paymentRepository.findByTenantId(tenantId, PageRequest.of(0, 10)))
            .thenReturn(page);

        Page<Payment> result = paymentService.findAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void shouldRecordPaymentForInvoice() {
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.recordPaymentForInvoice(invoice.getId(),
            new BigDecimal("50.00"), PaymentMethod.CARD, "TXN123", "Test");

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.getInvoice()).isEqualTo(invoice);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
    }

    @Test
    void shouldMarkInvoiceAsPaidWhenFullyPaid() {
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.recordPaymentForInvoice(invoice.getId(),
            new BigDecimal("100.00"), PaymentMethod.CARD, null, null);

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
    }

    @Test
    void shouldNotRecordZeroPaymentForInvoice() {
        when(invoiceRepository.findByIdAndTenantId(invoice.getId(), tenantId))
            .thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> paymentService.recordPaymentForInvoice(invoice.getId(),
            BigDecimal.ZERO, PaymentMethod.CARD, null, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("must be positive");
    }

    @Test
    void shouldRecordPaymentForOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
            .id(orderId)
            .tenantId(tenantId)
            .orderNumber("ORD-001")
            .build();

        when(orderService.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(orderService.recordPayment(any(), any())).thenReturn(order);

        Payment result = paymentService.recordPaymentForOrder(orderId,
            new BigDecimal("75.00"), PaymentMethod.BANK_TRANSFER, "REF123", null);

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.BANK_TRANSFER);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        verify(orderService).recordPayment(orderId, new BigDecimal("75.00"));
    }

    @Test
    void shouldRefundPayment() {
        payment.setIsRefund(false);
        when(paymentRepository.findByIdAndTenantId(payment.getId(), tenantId))
            .thenReturn(Optional.of(payment));
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.refund(payment.getId(), new BigDecimal("25.00"), "Customer request");

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(result.isRefundPayment()).isTrue();
        assertThat(result.getRefundOfPaymentId()).isEqualTo(payment.getId());
        assertThat(result.getNotes()).isEqualTo("Customer request");
    }

    @Test
    void shouldNotRefundRefundPayment() {
        payment.setIsRefund(true);
        when(paymentRepository.findByIdAndTenantId(payment.getId(), tenantId))
            .thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refund(payment.getId(), new BigDecimal("25.00"), "Reason"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot refund a refund");
    }

    @Test
    void shouldNotRefundMoreThanOriginalAmount() {
        payment.setAmount(new BigDecimal("50.00"));
        payment.setIsRefund(false);
        when(paymentRepository.findByIdAndTenantId(payment.getId(), tenantId))
            .thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refund(payment.getId(), new BigDecimal("100.00"), "Reason"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cannot exceed original");
    }

    @Test
    void shouldDeletePayment() {
        when(paymentRepository.findByIdAndTenantId(payment.getId(), tenantId))
            .thenReturn(Optional.of(payment));

        paymentService.delete(payment.getId());

        verify(paymentRepository).delete(payment);
    }

    @Test
    void shouldGeneratePaymentNumber() {
        when(paymentRepository.findLastPaymentNumber(tenantId)).thenReturn(null);

        String paymentNumber = paymentService.generatePaymentNumber();

        assertThat(paymentNumber).startsWith("PAY-");
        assertThat(paymentNumber).endsWith("-0001");
    }

    @Test
    void shouldIncrementPaymentNumberSequence() {
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        when(paymentRepository.findLastPaymentNumber(tenantId))
            .thenReturn("PAY-" + today + "-0003");

        String paymentNumber = paymentService.generatePaymentNumber();

        assertThat(paymentNumber).isEqualTo("PAY-" + today + "-0004");
    }

    @Test
    void shouldGetStatistics() {
        when(paymentRepository.countByTenantId(tenantId)).thenReturn(50L);
        when(paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.CASH)).thenReturn(10L);
        when(paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.CARD)).thenReturn(25L);
        when(paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.BANK_TRANSFER)).thenReturn(10L);
        when(paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.CHECK)).thenReturn(5L);
        when(paymentRepository.sumPaymentsTotal(tenantId)).thenReturn(new BigDecimal("10000.00"));
        when(paymentRepository.sumRefundsTotal(tenantId)).thenReturn(new BigDecimal("500.00"));

        Map<String, Object> stats = paymentService.getStatistics();

        assertThat(stats.get("total")).isEqualTo(50L);
        assertThat(stats.get("card")).isEqualTo(25L);
        assertThat(stats.get("totalPayments")).isEqualTo(new BigDecimal("10000.00"));
        assertThat(stats.get("totalRefunds")).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldFindByMethod() {
        Page<Payment> page = new PageImpl<>(List.of(payment));
        when(paymentRepository.findByTenantIdAndMethod(tenantId, PaymentMethod.CARD, PageRequest.of(0, 10)))
            .thenReturn(page);

        Page<Payment> result = paymentService.findByMethod(PaymentMethod.CARD, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMethod()).isEqualTo(PaymentMethod.CARD);
    }
}
