package com.sportsaas.billing.infra.service;

import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import com.sportsaas.billing.domain.service.PaymentService;
import com.sportsaas.billing.infra.repository.InvoiceRepository;
import com.sportsaas.billing.infra.repository.PaymentRepository;
import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.service.OrderService;
import com.sportsaas.tenant.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of PaymentService.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;

    @Override
    @Transactional
    public Payment create(Payment payment) {
        UUID tenantId = TenantContext.requireTenantId();
        payment.setTenantId(tenantId);

        if (payment.getPaymentNumber() == null) {
            payment.setPaymentNumber(generatePaymentNumber());
        }

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        log.info("Creating payment {} for tenant {}", payment.getPaymentNumber(), tenantId);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findById(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Payment not found: " + id));
    }

    @Override
    public Payment findByPaymentNumber(String paymentNumber) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByPaymentNumberAndTenantId(paymentNumber, tenantId)
            .orElseThrow(() -> new NotFoundException("Payment not found with number: " + paymentNumber));
    }

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Page<Payment> findByInvoiceId(UUID invoiceId, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByInvoiceIdAndTenantId(invoiceId, tenantId, pageable);
    }

    @Override
    public Page<Payment> findByOrderId(UUID orderId, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByOrderIdAndTenantId(orderId, tenantId, pageable);
    }

    @Override
    public Page<Payment> findByMethod(PaymentMethod method, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByTenantIdAndMethod(tenantId, method, pageable);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        Payment payment = paymentRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Payment not found: " + id));

        log.info("Deleting payment {}", payment.getPaymentNumber());
        paymentRepository.delete(payment);
    }

    @Override
    @Transactional
    public Payment recordPaymentForInvoice(UUID invoiceId, BigDecimal amount, PaymentMethod method,
                                           String transactionReference, String notes) {
        UUID tenantId = TenantContext.requireTenantId();

        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found: " + invoiceId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }

        Payment payment = Payment.builder()
            .tenantId(tenantId)
            .paymentNumber(generatePaymentNumber())
            .invoice(invoice)
            .orderId(invoice.getOrderId())
            .amount(amount)
            .method(method)
            .paymentDate(LocalDateTime.now())
            .transactionReference(transactionReference)
            .notes(notes)
            .isRefund(false)
            .build();

        payment = paymentRepository.save(payment);

        // Update invoice paid amount
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        log.info("Recorded payment {} of {} for invoice {}", payment.getPaymentNumber(), amount, invoice.getInvoiceNumber());
        return payment;
    }

    @Override
    @Transactional
    public Payment recordPaymentForOrder(UUID orderId, BigDecimal amount, PaymentMethod method,
                                         String transactionReference, String notes) {
        UUID tenantId = TenantContext.requireTenantId();

        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }

        Payment payment = Payment.builder()
            .tenantId(tenantId)
            .paymentNumber(generatePaymentNumber())
            .orderId(orderId)
            .amount(amount)
            .method(method)
            .paymentDate(LocalDateTime.now())
            .transactionReference(transactionReference)
            .notes(notes)
            .isRefund(false)
            .build();

        payment = paymentRepository.save(payment);

        // Update order paid amount
        orderService.recordPayment(orderId, amount);

        log.info("Recorded payment {} of {} for order {}", payment.getPaymentNumber(), amount, order.getOrderNumber());
        return payment;
    }

    @Override
    @Transactional
    public Payment refund(UUID paymentId, BigDecimal amount, String reason) {
        UUID tenantId = TenantContext.requireTenantId();

        Payment originalPayment = paymentRepository.findByIdAndTenantId(paymentId, tenantId)
            .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));

        if (originalPayment.isRefundPayment()) {
            throw new BusinessException("Cannot refund a refund payment");
        }

        if (amount.compareTo(originalPayment.getAmount()) > 0) {
            throw new BusinessException("Refund amount cannot exceed original payment amount");
        }

        Payment refundPayment = Payment.builder()
            .tenantId(tenantId)
            .paymentNumber(generatePaymentNumber())
            .invoice(originalPayment.getInvoice())
            .orderId(originalPayment.getOrderId())
            .amount(amount)
            .method(originalPayment.getMethod())
            .paymentDate(LocalDateTime.now())
            .notes(reason)
            .isRefund(true)
            .refundOfPaymentId(paymentId)
            .build();

        refundPayment = paymentRepository.save(refundPayment);

        // Update invoice if applicable
        if (originalPayment.getInvoice() != null) {
            Invoice invoice = originalPayment.getInvoice();
            BigDecimal newPaidAmount = invoice.getPaidAmount().subtract(amount);
            invoice.setPaidAmount(newPaidAmount.max(BigDecimal.ZERO));

            if (newPaidAmount.compareTo(BigDecimal.ZERO) <= 0) {
                invoice.setStatus(InvoiceStatus.REFUNDED);
            } else if (newPaidAmount.compareTo(invoice.getTotalAmount()) < 0) {
                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            }

            invoiceRepository.save(invoice);
        }

        // Update order if applicable
        if (originalPayment.getOrderId() != null) {
            orderService.refund(originalPayment.getOrderId(), amount);
        }

        log.info("Created refund {} of {} for payment {}", refundPayment.getPaymentNumber(), amount, originalPayment.getPaymentNumber());
        return refundPayment;
    }

    @Override
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    @Override
    public Page<Payment> findRefunds(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findRefundPayments(tenantId, pageable);
    }

    @Override
    public long count() {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.countByTenantId(tenantId);
    }

    @Override
    public Map<String, Object> getStatistics() {
        UUID tenantId = TenantContext.requireTenantId();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", paymentRepository.countByTenantId(tenantId));
        stats.put("cash", paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.CASH));
        stats.put("card", paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.CARD));
        stats.put("bankTransfer", paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.BANK_TRANSFER));
        stats.put("check", paymentRepository.countByTenantIdAndMethod(tenantId, PaymentMethod.CHECK));
        stats.put("totalPayments", paymentRepository.sumPaymentsTotal(tenantId));
        stats.put("totalRefunds", paymentRepository.sumRefundsTotal(tenantId));
        return stats;
    }

    @Override
    public String generatePaymentNumber() {
        UUID tenantId = TenantContext.requireTenantId();
        String prefix = "PAY";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastPaymentNumber = paymentRepository.findLastPaymentNumber(tenantId);

        int sequence = 1;
        if (lastPaymentNumber != null && lastPaymentNumber.contains(datePart)) {
            String seqPart = lastPaymentNumber.substring(lastPaymentNumber.length() - 4);
            try {
                sequence = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }

        return String.format("%s-%s-%04d", prefix, datePart, sequence);
    }
}
