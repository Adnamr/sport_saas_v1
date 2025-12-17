package com.sportsaas.order.infra.repository;

import com.sportsaas.order.domain.entity.Order;
import com.sportsaas.order.domain.enums.OrderStatus;
import com.sportsaas.order.domain.enums.OrderType;
import com.sportsaas.order.domain.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Order> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Order> findByOrderNumberAndTenantId(String orderNumber, UUID tenantId);

    Page<Order> findByTenantIdAndStatus(UUID tenantId, OrderStatus status, Pageable pageable);

    Page<Order> findByTenantIdAndType(UUID tenantId, OrderType type, Pageable pageable);

    Page<Order> findByTenantIdAndPaymentStatus(UUID tenantId, PaymentStatus paymentStatus, Pageable pageable);

    Page<Order> findByTenantIdAndCustomerId(UUID tenantId, UUID customerId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.type = :type AND o.status = :status")
    Page<Order> findByTenantIdAndTypeAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("type") OrderType type,
        @Param("status") OrderStatus status,
        Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.type = 'RENTAL' " +
           "AND o.status = 'IN_PROGRESS' AND o.rentalEndDate < :date")
    List<Order> findOverdueRentals(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.type = 'RENTAL' " +
           "AND o.status = 'IN_PROGRESS' AND o.rentalEndDate = :date")
    List<Order> findRentalsDueOn(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByTenantIdAndDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, OrderStatus status);

    long countByTenantIdAndType(UUID tenantId, OrderType type);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId AND o.type = 'RENTAL' " +
           "AND o.status = 'IN_PROGRESS' AND o.rentalEndDate < CURRENT_DATE")
    long countOverdueRentals(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.tenantId = :tenantId AND o.status = 'COMPLETED'")
    java.math.BigDecimal sumCompletedOrdersTotal(@Param("tenantId") UUID tenantId);

    @Query("SELECT MAX(o.orderNumber) FROM Order o WHERE o.tenantId = :tenantId")
    String findLastOrderNumber(@Param("tenantId") UUID tenantId);

    boolean existsByOrderNumberAndTenantId(String orderNumber, UUID tenantId);
}
