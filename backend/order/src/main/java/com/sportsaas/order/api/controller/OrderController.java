package com.sportsaas.order.api.controller;

import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.service.ProductService;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.api.dto.CancelOrderRequest;
import com.sportsaas.order.api.dto.CreateOrderItemRequest;
import com.sportsaas.order.api.dto.CreateOrderRequest;
import com.sportsaas.order.api.dto.OrderResponse;
import com.sportsaas.order.api.dto.PaymentRequest;
import com.sportsaas.order.api.dto.ReturnRentalRequest;
import com.sportsaas.order.api.dto.UpdateOrderRequest;
import com.sportsaas.order.api.mapper.OrderItemMapper;
import com.sportsaas.order.api.mapper.OrderMapper;
import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.entity.OrderItem;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import com.sportsaas.order.domain.service.OrderService;
import com.sportsaas.tenant.domain.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Order REST controller.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order and rental management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @GetMapping
    @Operation(summary = "List orders", description = "Get all orders for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<OrderResponse>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderType type,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) UUID customerId,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Order> page;

        if (status != null) {
            page = orderService.findByStatus(tenantId, status, pageable);
        } else if (type != null) {
            page = orderService.findByType(tenantId, type, pageable);
        } else if (paymentStatus != null) {
            page = orderService.findByPaymentStatus(tenantId, paymentStatus, pageable);
        } else if (customerId != null) {
            page = orderService.findByCustomerId(tenantId, customerId, pageable);
        } else {
            page = orderService.findByTenantId(tenantId, pageable);
        }

        Page<OrderResponse> responsePage = page.map(orderMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order", description = "Get an order by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new NotFoundException("Order not found: " + id));
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Get an order by order number")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderNumber));
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderMapper.toEntity(request);

        // Add items if provided
        if (request.items() != null && !request.items().isEmpty()) {
            for (CreateOrderItemRequest itemRequest : request.items()) {
                OrderItem item = createOrderItem(itemRequest);
                order.addItem(item);
            }
        }

        Order savedOrder = orderService.create(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(savedOrder));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order", description = "Update an existing order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest request) {
        Order order = orderService.findById(id)
            .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        orderMapper.updateEntity(request, order);
        Order updatedOrder = orderService.update(order);
        return ResponseEntity.ok(orderMapper.toResponse(updatedOrder));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order", description = "Delete an order (only draft or cancelled)")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Order lifecycle endpoints

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm order", description = "Confirm a draft or pending order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> confirm(@PathVariable UUID id) {
        Order order = orderService.confirm(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start processing", description = "Start processing a confirmed order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> startProcessing(@PathVariable UUID id) {
        Order order = orderService.startProcessing(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/ready")
    @Operation(summary = "Mark as ready", description = "Mark an order as ready for delivery/pickup")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> markReady(@PathVariable UUID id) {
        Order order = orderService.markReady(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Deliver order", description = "Mark an order as delivered")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> deliver(@PathVariable UUID id) {
        Order order = orderService.deliver(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete order", description = "Mark an order as completed")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> complete(@PathVariable UUID id) {
        Order order = orderService.complete(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request != null ? request.reason() : null;
        Order order = orderService.cancel(id, reason);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    // Rental specific endpoints

    @PostMapping("/{id}/return")
    @Operation(summary = "Return rental", description = "Return a rental order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> returnRental(
            @PathVariable UUID id,
            @RequestBody(required = false) ReturnRentalRequest request) {
        Order order = orderService.returnRental(id, request != null ? request.actualReturnDate() : null);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @GetMapping("/rentals/overdue")
    @Operation(summary = "Get overdue rentals", description = "Get all overdue rental orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getOverdueRentals() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Order> orders = orderService.findOverdueRentals(tenantId);
        List<OrderResponse> response = orders.stream()
            .map(orderMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rentals/due-today")
    @Operation(summary = "Get rentals due today", description = "Get all rentals due for return today")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getRentalsDueToday() {
        UUID tenantId = TenantContext.requireTenantId();
        List<Order> orders = orderService.findRentalsDueToday(tenantId);
        List<OrderResponse> response = orders.stream()
            .map(orderMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    // Order items endpoints

    @PostMapping("/{id}/items")
    @Operation(summary = "Add item", description = "Add an item to an order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOrderItemRequest request) {
        OrderItem item = createOrderItem(request);
        Order order = orderService.addItem(id, item);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Remove item", description = "Remove an item from an order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId) {
        Order order = orderService.removeItem(orderId, itemId);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PutMapping("/{orderId}/items/{itemId}/quantity")
    @Operation(summary = "Update item quantity", description = "Update the quantity of an order item")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> updateItemQuantity(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @RequestParam int quantity) {
        Order order = orderService.updateItemQuantity(orderId, itemId, quantity);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    // Payment endpoints

    @PostMapping("/{id}/payments")
    @Operation(summary = "Record payment", description = "Record a payment for an order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> recordPayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRequest request) {
        Order order = orderService.recordPayment(id, request.amount());
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund payment", description = "Refund a payment for an order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER')")
    public ResponseEntity<OrderResponse> refund(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRequest request) {
        Order order = orderService.refund(id, request.amount());
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PostMapping("/{id}/deposit/return")
    @Operation(summary = "Return deposit", description = "Return the deposit for a rental order")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> returnDeposit(@PathVariable UUID id) {
        Order order = orderService.returnDeposit(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    // Statistics endpoint

    @GetMapping("/stats")
    @Operation(summary = "Get order statistics", description = "Get order statistics for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getStats() {
        UUID tenantId = TenantContext.requireTenantId();
        return ResponseEntity.ok(orderService.getStatistics(tenantId));
    }

    private OrderItem createOrderItem(CreateOrderItemRequest request) {
        Product product = productService.findById(request.productId())
            .orElseThrow(() -> new NotFoundException("Product not found: " + request.productId()));

        OrderItem item = orderItemMapper.toEntity(request);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductSku(product.getSku());
        item.calculateTotalPrice();
        return item;
    }
}
