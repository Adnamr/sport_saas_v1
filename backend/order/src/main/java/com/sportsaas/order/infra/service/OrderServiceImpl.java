package com.sportsaas.order.infra.service;

import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.entity.OrderItem;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import com.sportsaas.order.domain.service.OrderService;
import com.sportsaas.order.infra.repository.OrderItemRepository;
import com.sportsaas.order.infra.repository.OrderRepository;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of OrderService.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public Order create(Order order) {
        UUID tenantId = TenantContext.requireTenantId();
        order.setTenantId(tenantId);

        if (order.getOrderNumber() == null) {
            order.setOrderNumber(generateOrderNumber(tenantId));
        }

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.DRAFT);
        }

        order.recalculateTotals();
        log.info("Creating order {} for tenant {}", order.getOrderNumber(), tenantId);
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        return orderRepository.findByIdAndTenantId(id, tenantId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        UUID tenantId = TenantContext.requireTenantId();
        return orderRepository.findByOrderNumberAndTenantId(orderNumber, tenantId);
    }

    @Override
    public Page<Order> findByTenantId(UUID tenantId, Pageable pageable) {
        return orderRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional
    public Order update(Order order) {
        UUID tenantId = TenantContext.requireTenantId();
        Order existing = orderRepository.findByIdAndTenantId(order.getId(), tenantId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + order.getId()));

        existing.setCustomerName(order.getCustomerName());
        existing.setCustomerEmail(order.getCustomerEmail());
        existing.setCustomerPhone(order.getCustomerPhone());
        existing.setNotes(order.getNotes());
        existing.setTaxAmount(order.getTaxAmount());
        existing.setDiscountAmount(order.getDiscountAmount());
        existing.setRentalStartDate(order.getRentalStartDate());
        existing.setRentalEndDate(order.getRentalEndDate());
        existing.setDepositAmount(order.getDepositAmount());
        existing.recalculateTotals();

        log.info("Updating order {}", existing.getOrderNumber());
        return orderRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        Order order = orderRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot delete order in status: " + order.getStatus());
        }

        log.info("Deleting order {}", order.getOrderNumber());
        orderRepository.delete(order);
    }

    @Override
    public Page<Order> findByStatus(UUID tenantId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    public Page<Order> findByType(UUID tenantId, OrderType type, Pageable pageable) {
        return orderRepository.findByTenantIdAndType(tenantId, type, pageable);
    }

    @Override
    public Page<Order> findByPaymentStatus(UUID tenantId, PaymentStatus paymentStatus, Pageable pageable) {
        return orderRepository.findByTenantIdAndPaymentStatus(tenantId, paymentStatus, pageable);
    }

    @Override
    public Page<Order> findByCustomerId(UUID tenantId, UUID customerId, Pageable pageable) {
        return orderRepository.findByTenantIdAndCustomerId(tenantId, customerId, pageable);
    }

    @Override
    @Transactional
    public Order confirm(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        if (!order.canConfirm()) {
            throw new BusinessException("Order cannot be confirmed in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        log.info("Order {} confirmed", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order startProcessing(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Order must be confirmed before processing");
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        log.info("Order {} started processing", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order markReady(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessException("Order must be in progress to mark as ready");
        }

        order.setStatus(OrderStatus.READY);
        log.info("Order {} marked as ready", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order deliver(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        if (order.getStatus() != OrderStatus.READY && order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessException("Order must be ready or in progress to deliver");
        }

        order.setStatus(OrderStatus.DELIVERED);

        if (!order.isRental()) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());
        }

        log.info("Order {} delivered", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order complete(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        log.info("Order {} completed", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancel(UUID orderId, String reason) {
        Order order = getOrderForUpdate(orderId);

        if (!order.canCancel()) {
            throw new BusinessException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        log.info("Order {} cancelled: {}", order.getOrderNumber(), reason);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order returnRental(UUID orderId, LocalDate actualReturnDate) {
        Order order = getOrderForUpdate(orderId);

        if (!order.isRental()) {
            throw new BusinessException("Order is not a rental");
        }

        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new BusinessException("Rental cannot be returned in status: " + order.getStatus());
        }

        order.setActualReturnDate(actualReturnDate != null ? actualReturnDate : LocalDate.now());
        order.setStatus(OrderStatus.RETURNED);
        order.setCompletedAt(LocalDateTime.now());

        log.info("Rental {} returned on {}", order.getOrderNumber(), order.getActualReturnDate());
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findOverdueRentals(UUID tenantId) {
        return orderRepository.findOverdueRentals(tenantId, LocalDate.now());
    }

    @Override
    public List<Order> findRentalsDueToday(UUID tenantId) {
        return orderRepository.findRentalsDueOn(tenantId, LocalDate.now());
    }

    @Override
    @Transactional
    public Order addItem(UUID orderId, OrderItem item) {
        Order order = getOrderForUpdate(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Cannot modify order in status: " + order.getStatus());
        }

        item.setTenantId(order.getTenantId());
        item.calculateTotalPrice();
        order.addItem(item);

        log.info("Added item to order {}", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order removeItem(UUID orderId, UUID itemId) {
        Order order = getOrderForUpdate(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Cannot modify order in status: " + order.getStatus());
        }

        OrderItem item = order.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Order item not found: " + itemId));

        order.removeItem(item);
        orderItemRepository.delete(item);

        log.info("Removed item from order {}", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateItemQuantity(UUID orderId, UUID itemId, int quantity) {
        Order order = getOrderForUpdate(orderId);

        if (order.getStatus() != OrderStatus.DRAFT) {
            throw new BusinessException("Cannot modify order in status: " + order.getStatus());
        }

        OrderItem item = order.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Order item not found: " + itemId));

        item.setQuantity(quantity);
        item.calculateTotalPrice();
        order.recalculateTotals();

        log.info("Updated item quantity in order {}", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order recordPayment(UUID orderId, BigDecimal amount) {
        Order order = getOrderForUpdate(orderId);

        BigDecimal newPaidAmount = order.getPaidAmount().add(amount);
        order.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(order.getTotalAmount()) >= 0) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        log.info("Recorded payment of {} for order {}", amount, order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order refund(UUID orderId, BigDecimal amount) {
        Order order = getOrderForUpdate(orderId);

        BigDecimal newPaidAmount = order.getPaidAmount().subtract(amount);
        if (newPaidAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Refund amount exceeds paid amount");
        }

        order.setPaidAmount(newPaidAmount);

        if (newPaidAmount.compareTo(BigDecimal.ZERO) == 0) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        log.info("Refunded {} for order {}", amount, order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order returnDeposit(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        if (!order.isRental()) {
            throw new BusinessException("Order is not a rental");
        }

        if (order.getDepositAmount() == null || order.getDepositAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("No deposit to return");
        }

        order.setDepositReturned(true);
        log.info("Deposit returned for order {}", order.getOrderNumber());
        return orderRepository.save(order);
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return orderRepository.countByTenantId(tenantId);
    }

    @Override
    public long countByStatus(UUID tenantId, OrderStatus status) {
        return orderRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public Map<String, Object> getStatistics(UUID tenantId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", orderRepository.countByTenantId(tenantId));
        stats.put("draft", orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.DRAFT));
        stats.put("pending", orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.PENDING));
        stats.put("confirmed", orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.CONFIRMED));
        stats.put("inProgress", orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.IN_PROGRESS));
        stats.put("completed", orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.COMPLETED));
        stats.put("cancelled", orderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.CANCELLED));
        stats.put("sales", orderRepository.countByTenantIdAndType(tenantId, OrderType.SALE));
        stats.put("rentals", orderRepository.countByTenantIdAndType(tenantId, OrderType.RENTAL));
        stats.put("overdueRentals", orderRepository.countOverdueRentals(tenantId));
        stats.put("totalRevenue", orderRepository.sumCompletedOrdersTotal(tenantId));
        return stats;
    }

    @Override
    public String generateOrderNumber(UUID tenantId) {
        String prefix = "ORD";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String lastOrderNumber = orderRepository.findLastOrderNumber(tenantId);

        int sequence = 1;
        if (lastOrderNumber != null && lastOrderNumber.contains(datePart)) {
            String seqPart = lastOrderNumber.substring(lastOrderNumber.length() - 4);
            try {
                sequence = Integer.parseInt(seqPart) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }

        return String.format("%s-%s-%04d", prefix, datePart, sequence);
    }

    private Order getOrderForUpdate(UUID orderId) {
        UUID tenantId = TenantContext.requireTenantId();
        return orderRepository.findByIdAndTenantId(orderId, tenantId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }
}
