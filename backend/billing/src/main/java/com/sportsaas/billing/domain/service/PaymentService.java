package com.sportsaas.billing.domain.service;

import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for Payment operations.
 */
public interface PaymentService {

    // CRUD operations
    Payment create(Payment payment);

    Payment findById(UUID id);

    Payment findByPaymentNumber(String paymentNumber);

    Page<Payment> findAll(Pageable pageable);

    Page<Payment> findByInvoiceId(UUID invoiceId, Pageable pageable);

    Page<Payment> findByOrderId(UUID orderId, Pageable pageable);

    Page<Payment> findByMethod(PaymentMethod method, Pageable pageable);

    void delete(UUID id);

    // Payment for invoice
    Payment recordPaymentForInvoice(UUID invoiceId, BigDecimal amount, PaymentMethod method,
                                    String transactionReference, String notes);

    // Payment for order
    Payment recordPaymentForOrder(UUID orderId, BigDecimal amount, PaymentMethod method,
                                  String transactionReference, String notes);

    // Refund
    Payment refund(UUID paymentId, BigDecimal amount, String reason);

    // Queries
    List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Page<Payment> findRefunds(Pageable pageable);

    // Statistics
    long count();

    Map<String, Object> getStatistics();

    // Payment number generation
    String generatePaymentNumber();
}
