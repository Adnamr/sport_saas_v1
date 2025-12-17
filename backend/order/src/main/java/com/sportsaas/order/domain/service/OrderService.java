package com.sportsaas.order.domain.service;

import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.entity.OrderItem;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Order operations.
 */
public interface OrderService {

    // CRUD operations
    Order create(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByTenantId(UUID tenantId, Pageable pageable);

    Order update(Order order);

    void delete(UUID id);

    // Filtered queries
    Page<Order> findByStatus(UUID tenantId, OrderStatus status, Pageable pageable);

    Page<Order> findByType(UUID tenantId, OrderType type, Pageable pageable);

    Page<Order> findByPaymentStatus(UUID tenantId, PaymentStatus paymentStatus, Pageable pageable);

    Page<Order> findByCustomerId(UUID tenantId, UUID customerId, Pageable pageable);

    // Order lifecycle
    Order confirm(UUID orderId);

    Order startProcessing(UUID orderId);

    Order markReady(UUID orderId);

    Order deliver(UUID orderId);

    Order complete(UUID orderId);

    Order cancel(UUID orderId, String reason);

    // Rental specific
    Order returnRental(UUID orderId, LocalDate actualReturnDate);

    List<Order> findOverdueRentals(UUID tenantId);

    List<Order> findRentalsDueToday(UUID tenantId);

    // Order items
    Order addItem(UUID orderId, OrderItem item);

    Order removeItem(UUID orderId, UUID itemId);

    Order updateItemQuantity(UUID orderId, UUID itemId, int quantity);

    // Payments
    Order recordPayment(UUID orderId, BigDecimal amount);

    Order refund(UUID orderId, BigDecimal amount);

    Order returnDeposit(UUID orderId);

    // Statistics
    long countByTenantId(UUID tenantId);

    long countByStatus(UUID tenantId, OrderStatus status);

    Map<String, Object> getStatistics(UUID tenantId);

    // Order number generation
    String generateOrderNumber(UUID tenantId);
}
