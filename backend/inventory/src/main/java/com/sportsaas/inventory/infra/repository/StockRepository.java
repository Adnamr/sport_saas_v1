package com.sportsaas.inventory.infra.repository;

import com.sportsaas.inventory.domain.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Stock entity.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {

    Page<Stock> findByTenantId(UUID tenantId, Pageable pageable);

    List<Stock> findByTenantIdAndProductId(UUID tenantId, UUID productId);

    Optional<Stock> findByTenantIdAndProductIdAndWarehouseId(UUID tenantId, UUID productId, UUID warehouseId);

    Page<Stock> findByTenantIdAndWarehouseId(UUID tenantId, UUID warehouseId, Pageable pageable);

    @Query("SELECT s FROM Stock s WHERE s.tenantId = :tenantId " +
           "AND (s.quantity - s.reservedQuantity) <= s.alertThreshold")
    List<Stock> findLowStockByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT s FROM Stock s WHERE s.tenantId = :tenantId " +
           "AND (s.quantity - s.reservedQuantity) <= s.alertThreshold")
    Page<Stock> findLowStockByTenantId(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT s FROM Stock s JOIN FETCH s.product p JOIN FETCH s.warehouse w " +
           "WHERE s.tenantId = :tenantId")
    Page<Stock> findByTenantIdWithDetails(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Stock s WHERE s.tenantId = :tenantId AND s.product.id = :productId")
    Integer getTotalQuantityByProduct(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);

    @Query("SELECT COALESCE(SUM(s.quantity - s.reservedQuantity), 0) FROM Stock s " +
           "WHERE s.tenantId = :tenantId AND s.product.id = :productId")
    Integer getAvailableQuantityByProduct(@Param("tenantId") UUID tenantId, @Param("productId") UUID productId);

    long countByTenantId(UUID tenantId);
}
