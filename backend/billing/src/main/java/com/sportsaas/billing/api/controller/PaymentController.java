package com.sportsaas.billing.api.controller;

import com.sportsaas.billing.api.dto.CreatePaymentRequest;
import com.sportsaas.billing.api.dto.PaymentResponse;
import com.sportsaas.billing.api.dto.RefundRequest;
import com.sportsaas.billing.api.mapper.PaymentMapper;
import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import com.sportsaas.billing.domain.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing payments.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all payments", description = "Get paginated list of payments")
    public Page<PaymentResponse> list(Pageable pageable) {
        return paymentService.findAll(pageable).map(paymentMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment by ID", description = "Get a specific payment by its ID")
    public PaymentResponse getById(@PathVariable UUID id) {
        return paymentMapper.toResponse(paymentService.findById(id));
    }

    @GetMapping("/number/{paymentNumber}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment by number", description = "Get a specific payment by its number")
    public PaymentResponse getByNumber(@PathVariable String paymentNumber) {
        return paymentMapper.toResponse(paymentService.findByPaymentNumber(paymentNumber));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payments by invoice", description = "Get all payments for a specific invoice")
    public Page<PaymentResponse> getByInvoiceId(@PathVariable UUID invoiceId, Pageable pageable) {
        return paymentService.findByInvoiceId(invoiceId, pageable).map(paymentMapper::toResponse);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payments by order", description = "Get all payments for a specific order")
    public Page<PaymentResponse> getByOrderId(@PathVariable UUID orderId, Pageable pageable) {
        return paymentService.findByOrderId(orderId, pageable).map(paymentMapper::toResponse);
    }

    @GetMapping("/method/{method}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payments by method", description = "Get all payments with a specific method")
    public Page<PaymentResponse> getByMethod(@PathVariable PaymentMethod method, Pageable pageable) {
        return paymentService.findByMethod(method, pageable).map(paymentMapper::toResponse);
    }

    @PostMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record payment for invoice", description = "Record a new payment for an invoice")
    public PaymentResponse recordForInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.recordPaymentForInvoice(invoiceId, request.amount(), request.method(),
                request.transactionReference(), request.notes());
        return paymentMapper.toResponse(payment);
    }

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record payment for order", description = "Record a new payment directly for an order")
    public PaymentResponse recordForOrder(@PathVariable UUID orderId, @Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.recordPaymentForOrder(orderId, request.amount(), request.method(),
                request.transactionReference(), request.notes());
        return paymentMapper.toResponse(payment);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Refund payment", description = "Create a refund for an existing payment")
    public PaymentResponse refund(@PathVariable UUID id, @Valid @RequestBody RefundRequest request) {
        Payment refund = paymentService.refund(id, request.amount(), request.reason());
        return paymentMapper.toResponse(refund);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete payment", description = "Delete a payment")
    public void delete(@PathVariable UUID id) {
        paymentService.delete(id);
    }
}
