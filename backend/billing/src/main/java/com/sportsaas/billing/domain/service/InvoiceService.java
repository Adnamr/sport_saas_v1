package com.sportsaas.billing.domain.service;

import com.sportsaas.billing.api.dto.CreateInvoiceItemRequest;
import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.InvoiceItem;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for Invoice operations.
 */
public interface InvoiceService {

    // CRUD operations
    Invoice create(Invoice invoice, List<CreateInvoiceItemRequest> items);

    Invoice findById(UUID id);

    Invoice findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findAll(Pageable pageable);

    Page<Invoice> findByOrderId(UUID orderId, Pageable pageable);

    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Page<Invoice> findOverdue(Pageable pageable);

    Invoice update(UUID id, Invoice invoice, List<CreateInvoiceItemRequest> items);

    void delete(UUID id);

    // Invoice lifecycle
    Invoice send(UUID invoiceId);

    Invoice markAsPaid(UUID invoiceId);

    Invoice cancel(UUID invoiceId);

    // Invoice items
    Invoice addItem(UUID invoiceId, InvoiceItem item);

    Invoice removeItem(UUID invoiceId, UUID itemId);

    // Payment recording
    Payment recordPayment(UUID invoiceId, BigDecimal amount, PaymentMethod method,
                          String transactionReference, String notes);

    Page<Payment> findPaymentsByInvoice(UUID invoiceId, Pageable pageable);

    // Queries
    List<Invoice> findOverdueInvoices();

    List<Invoice> findInvoicesDueToday();

    void updateOverdueStatuses();

    // Statistics
    long count();

    long countByStatus(InvoiceStatus status);

    Map<String, Object> getStatistics();

    // Invoice number generation
    String generateInvoiceNumber();

    // Create from order
    Invoice createFromOrder(UUID orderId);
}
