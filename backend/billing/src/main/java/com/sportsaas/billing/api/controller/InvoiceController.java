package com.sportsaas.billing.api.controller;

import com.sportsaas.billing.api.dto.CreateInvoiceRequest;
import com.sportsaas.billing.api.dto.CreatePaymentRequest;
import com.sportsaas.billing.api.dto.InvoiceResponse;
import com.sportsaas.billing.api.dto.PaymentResponse;
import com.sportsaas.billing.api.mapper.InvoiceMapper;
import com.sportsaas.billing.api.mapper.PaymentMapper;
import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import com.sportsaas.billing.domain.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing invoices.
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all invoices", description = "Get paginated list of invoices")
    public Page<InvoiceResponse> list(Pageable pageable) {
        return invoiceService.findAll(pageable).map(invoiceMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoice by ID", description = "Get a specific invoice by its ID")
    public InvoiceResponse getById(@PathVariable UUID id) {
        return invoiceMapper.toResponse(invoiceService.findById(id));
    }

    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoice by number", description = "Get a specific invoice by its number")
    public InvoiceResponse getByNumber(@PathVariable String invoiceNumber) {
        return invoiceMapper.toResponse(invoiceService.findByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoices by order", description = "Get all invoices for a specific order")
    public Page<InvoiceResponse> getByOrderId(@PathVariable UUID orderId, Pageable pageable) {
        return invoiceService.findByOrderId(orderId, pageable).map(invoiceMapper::toResponse);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoices by customer", description = "Get all invoices for a specific customer")
    public Page<InvoiceResponse> getByCustomerId(@PathVariable UUID customerId, Pageable pageable) {
        return invoiceService.findByCustomerId(customerId, pageable).map(invoiceMapper::toResponse);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoices by status", description = "Get all invoices with a specific status")
    public Page<InvoiceResponse> getByStatus(@PathVariable InvoiceStatus status, Pageable pageable) {
        return invoiceService.findByStatus(status, pageable).map(invoiceMapper::toResponse);
    }

    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get overdue invoices", description = "Get all overdue invoices")
    public Page<InvoiceResponse> getOverdue(Pageable pageable) {
        return invoiceService.findOverdue(pageable).map(invoiceMapper::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create invoice", description = "Create a new invoice")
    public InvoiceResponse create(@Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        return invoiceMapper.toResponse(invoiceService.create(invoice, request.items()));
    }

    @PostMapping("/from-order/{orderId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create invoice from order", description = "Create a new invoice from an existing order")
    public InvoiceResponse createFromOrder(@PathVariable UUID orderId) {
        return invoiceMapper.toResponse(invoiceService.createFromOrder(orderId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Update invoice", description = "Update an existing invoice")
    public InvoiceResponse update(@PathVariable UUID id, @Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = invoiceMapper.toEntity(request);
        return invoiceMapper.toResponse(invoiceService.update(id, invoice, request.items()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete invoice", description = "Delete an invoice (only if in DRAFT status)")
    public void delete(@PathVariable UUID id) {
        invoiceService.delete(id);
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Send invoice", description = "Mark invoice as sent to customer")
    public InvoiceResponse send(@PathVariable UUID id) {
        return invoiceMapper.toResponse(invoiceService.send(id));
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @Operation(summary = "Mark invoice as paid", description = "Mark invoice as fully paid")
    public InvoiceResponse markAsPaid(@PathVariable UUID id) {
        return invoiceMapper.toResponse(invoiceService.markAsPaid(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Cancel invoice", description = "Cancel an invoice")
    public InvoiceResponse cancel(@PathVariable UUID id) {
        return invoiceMapper.toResponse(invoiceService.cancel(id));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record payment", description = "Record a payment for this invoice")
    public PaymentResponse recordPayment(@PathVariable UUID id, @Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = invoiceService.recordPayment(id, request.amount(), request.method(),
                request.transactionReference(), request.notes());
        return paymentMapper.toResponse(payment);
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get invoice payments", description = "Get all payments for this invoice")
    public Page<PaymentResponse> getPayments(@PathVariable UUID id, Pageable pageable) {
        Invoice invoice = invoiceService.findById(id);
        return invoiceService.findPaymentsByInvoice(invoice.getId(), pageable).map(paymentMapper::toResponse);
    }
}
