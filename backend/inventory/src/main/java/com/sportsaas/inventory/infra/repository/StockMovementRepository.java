package com.sportsaas.inventory.infra.repository;

import com.sportsaas.inventory.domain.entity.StockMovement;
import com.sportsaas.inventory.domain.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for StockMovement entity.
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findByTenantId(UUID tenantId, Pageable pageable);

    Page<StockMovement> findByStockId(UUID stockId, Pageable pageable);

    Page<StockMovement> findByTenantIdAndType(UUID tenantId, MovementType type, Pageable pageable);

    List<StockMovement> findByOrderId(UUID orderId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.tenantId = :tenantId " +
           "AND sm.createdAt BETWEEN :startDate AND :endDate ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByTenantIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm JOIN FETCH sm.stock s " +
           "WHERE sm.tenantId = :tenantId ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByTenantIdWithStock(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.stock.product.id = :productId " +
           "ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByProductId(@Param("productId") UUID productId, Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndType(UUID tenantId, MovementType type);
}
