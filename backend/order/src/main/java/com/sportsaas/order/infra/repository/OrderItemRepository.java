package com.sportsaas.order.infra.repository;

import com.sportsaas.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OrderItem entity.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    List<OrderItem> findByTenantId(UUID tenantId);

    Optional<OrderItem> findByIdAndTenantId(UUID id, UUID tenantId);

    List<OrderItem> findByProductId(UUID productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.tenantId = :tenantId AND oi.product.id = :productId")
    List<OrderItem> findByTenantIdAndProductId(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.reservationId = :reservationId")
    Optional<OrderItem> findByReservationId(@Param("reservationId") UUID reservationId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi " +
           "WHERE oi.tenantId = :tenantId AND oi.product.id = :productId")
    Long sumQuantityByTenantIdAndProductId(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);

    void deleteByOrderId(UUID orderId);
}
