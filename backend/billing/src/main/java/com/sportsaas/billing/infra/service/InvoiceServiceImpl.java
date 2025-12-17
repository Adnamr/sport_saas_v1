package com.sportsaas.billing.infra.service;

import com.sportsaas.billing.api.dto.CreateInvoiceItemRequest;
import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.InvoiceItem;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import com.sportsaas.billing.domain.service.InvoiceService;
import com.sportsaas.billing.infra.repository.InvoiceItemRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of InvoiceService.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Override
    @Transactional
    public Invoice create(Invoice invoice, List<CreateInvoiceItemRequest> items) {
        UUID tenantId = TenantContext.requireTenantId();
        invoice.setTenantId(tenantId);

        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(generateInvoiceNumber());
        }

        if (invoice.getStatus() == null) {
            invoice.setStatus(InvoiceStatus.DRAFT);
        }

        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }

        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getIssueDate().plusDays(30));
        }

        if (invoice.getItems() == null) {
            invoice.setItems(new ArrayList<>());
        }

        // Add items
        if (items != null) {
            for (CreateInvoiceItemRequest itemRequest : items) {
                InvoiceItem item = InvoiceItem.builder()
                    .tenantId(tenantId)
                    .productId(itemRequest.productId())
                    .description(itemRequest.description())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .discountPercent(itemRequest.discountPercent() != null ? itemRequest.discountPercent() : BigDecimal.ZERO)
                    .build();
                item.calculateTotalPrice();
                invoice.addItem(item);
            }
        }

        invoice.recalculateTotals();
        log.info("Creating invoice {} for tenant {}", invoice.getInvoiceNumber(), tenantId);
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice findById(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found: " + id));
    }

    @Override
    public Invoice findByInvoiceNumber(String invoiceNumber) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByInvoiceNumberAndTenantId(invoiceNumber, tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found with number: " + invoiceNumber));
    }

    @Override
    public Page<Invoice> findAll(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Page<Invoice> findByOrderId(UUID orderId, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByOrderIdAndTenantId(orderId, tenantId, pageable);
    }

    @Override
    public Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByTenantIdAndCustomerId(tenantId, customerId, pageable);
    }

    @Override
    public Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    public Page<Invoice> findOverdue(Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findOverdueInvoicesPaged(tenantId, LocalDate.now(), pageable);
    }

    @Override
    @Transactional
    public Invoice update(UUID id, Invoice invoice, List<CreateInvoiceItemRequest> items) {
        UUID tenantId = TenantContext.requireTenantId();
        Invoice existing = invoiceRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found: " + id));

        if (existing.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Cannot modify invoice in status: " + existing.getStatus());
        }

        existing.setCustomerId(invoice.getCustomerId());
        existing.setCustomerName(invoice.getCustomerName());
        existing.setCustomerEmail(invoice.getCustomerEmail());
        existing.setCustomerAddress(invoice.getCustomerAddress());
        existing.setCustomerTaxId(invoice.getCustomerTaxId());
        existing.setDueDate(invoice.getDueDate());
        existing.setTaxRate(invoice.getTaxRate());
        existing.setDiscountAmount(invoice.getDiscountAmount());
        existing.setNotes(invoice.getNotes());

        // Clear existing items and add new ones
        if (items != null) {
            existing.getItems().clear();
            for (CreateInvoiceItemRequest itemRequest : items) {
                InvoiceItem item = InvoiceItem.builder()
                    .tenantId(tenantId)
                    .productId(itemRequest.productId())
                    .description(itemRequest.description())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .discountPercent(itemRequest.discountPercent() != null ? itemRequest.discountPercent() : BigDecimal.ZERO)
                    .build();
                item.calculateTotalPrice();
                existing.addItem(item);
            }
        }

        existing.recalculateTotals();

        log.info("Updating invoice {}", existing.getInvoiceNumber());
        return invoiceRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        Invoice invoice = invoiceRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found: " + id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT && invoice.getStatus() != InvoiceStatus.CANCELLED) {
            throw new BusinessException("Cannot delete invoice in status: " + invoice.getStatus());
        }

        log.info("Deleting invoice {}", invoice.getInvoiceNumber());
        invoiceRepository.delete(invoice);
    }

    @Override
    @Transactional
    public Invoice send(UUID invoiceId) {
        Invoice invoice = getInvoiceForUpdate(invoiceId);

        if (!invoice.canSend()) {
            throw new BusinessException("Invoice cannot be sent in status: " + invoice.getStatus());
        }

        invoice.setStatus(InvoiceStatus.SENT);
        invoice.setSentAt(LocalDateTime.now());

        log.info("Invoice {} sent", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice markAsPaid(UUID invoiceId) {
        Invoice invoice = getInvoiceForUpdate(invoiceId);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaidAmount(invoice.getTotalAmount());

        log.info("Invoice {} marked as paid", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice cancel(UUID invoiceId) {
        Invoice invoice = getInvoiceForUpdate(invoiceId);

        if (!invoice.canCancel()) {
            throw new BusinessException("Invoice cannot be cancelled in status: " + invoice.getStatus());
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setCancelledAt(LocalDateTime.now());

        log.info("Invoice {} cancelled", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice addItem(UUID invoiceId, InvoiceItem item) {
        Invoice invoice = getInvoiceForUpdate(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Cannot modify invoice in status: " + invoice.getStatus());
        }

        item.setTenantId(invoice.getTenantId());
        item.calculateTotalPrice();
        invoice.addItem(item);

        log.info("Added item to invoice {}", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Invoice removeItem(UUID invoiceId, UUID itemId) {
        Invoice invoice = getInvoiceForUpdate(invoiceId);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Cannot modify invoice in status: " + invoice.getStatus());
        }

        InvoiceItem item = invoice.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Invoice item not found: " + itemId));

        invoice.removeItem(item);
        invoiceItemRepository.delete(item);

        log.info("Removed item from invoice {}", invoice.getInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public Payment recordPayment(UUID invoiceId, BigDecimal amount, PaymentMethod method,
                                 String transactionReference, String notes) {
        UUID tenantId = TenantContext.requireTenantId();
        Invoice invoice = getInvoiceForUpdate(invoiceId);

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
    public Page<Payment> findPaymentsByInvoice(UUID invoiceId, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        return paymentRepository.findByInvoiceIdAndTenantId(invoiceId, tenantId, pageable);
    }

    @Override
    public List<Invoice> findOverdueInvoices() {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findOverdueInvoices(tenantId, LocalDate.now());
    }

    @Override
    public List<Invoice> findInvoicesDueToday() {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findInvoicesDueOn(tenantId, LocalDate.now());
    }

    @Override
    @Transactional
    public void updateOverdueStatuses() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Invoice> overdueInvoices = findOverdueInvoices();
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getStatus() != InvoiceStatus.OVERDUE) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
                log.info("Invoice {} marked as overdue", invoice.getInvoiceNumber());
            }
        }
    }

    @Override
    public long count() {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.countByTenantId(tenantId);
    }

    @Override
    public long countByStatus(InvoiceStatus status) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public Map<String, Object> getStatistics() {
        UUID tenantId = TenantContext.requireTenantId();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", invoiceRepository.countByTenantId(tenantId));
        stats.put("draft", invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.DRAFT));
        stats.put("sent", invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.SENT));
        stats.put("paid", invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.PAID));
        stats.put("partiallyPaid", invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.PARTIALLY_PAID));
        stats.put("overdue", invoiceRepository.countOverdueInvoices(tenantId));
        stats.put("cancelled", invoiceRepository.countByTenantIdAndStatus(tenantId, InvoiceStatus.CANCELLED));
        stats.put("totalPaid", invoiceRepository.sumPaidInvoicesTotal(tenantId));
        stats.put("totalOutstanding", invoiceRepository.sumOutstandingAmount(tenantId));
        return stats;
    }

    @Override
    public String generateInvoiceNumber() {
        UUID tenantId = TenantContext.requireTenantId();
        String prefix = "INV";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastInvoiceNumber = invoiceRepository.findLastInvoiceNumber(tenantId);

        int sequence = 1;
        if (lastInvoiceNumber != null && lastInvoiceNumber.contains(datePart)) {
            String seqPart = lastInvoiceNumber.substring(lastInvoiceNumber.length() - 4);
            try {
                sequence = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }

        return String.format("%s-%s-%04d", prefix, datePart, sequence);
    }

    @Override
    @Transactional
    public Invoice createFromOrder(UUID orderId) {
        UUID tenantId = TenantContext.requireTenantId();

        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        // Check if invoice already exists for this order
        Optional<Invoice> existingInvoice = invoiceRepository.findFirstByOrderIdAndTenantId(orderId, tenantId);
        if (existingInvoice.isPresent()) {
            throw new BusinessException("Invoice already exists for order: " + orderId);
        }

        Invoice invoice = Invoice.builder()
            .tenantId(tenantId)
            .invoiceNumber(generateInvoiceNumber())
            .orderId(orderId)
            .customerId(order.getCustomerId())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .issueDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .subtotal(order.getSubtotal())
            .taxAmount(order.getTaxAmount())
            .discountAmount(order.getDiscountAmount())
            .totalAmount(order.getTotalAmount())
            .status(InvoiceStatus.DRAFT)
            .build();

        // Create invoice items from order items
        order.getItems().forEach(orderItem -> {
            InvoiceItem invoiceItem = InvoiceItem.builder()
                .tenantId(tenantId)
                .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                .description(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .discountPercent(orderItem.getDiscountPercent())
                .totalPrice(orderItem.getTotalPrice())
                .build();
            invoice.addItem(invoiceItem);
        });

        log.info("Creating invoice {} from order {}", invoice.getInvoiceNumber(), orderId);
        return invoiceRepository.save(invoice);
    }

    private String generatePaymentNumber() {
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

    private Invoice getInvoiceForUpdate(UUID invoiceId) {
        UUID tenantId = TenantContext.requireTenantId();
        return invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
            .orElseThrow(() -> new NotFoundException("Invoice not found: " + invoiceId));
    }
}
