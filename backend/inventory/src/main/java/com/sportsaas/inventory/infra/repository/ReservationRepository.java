package com.sportsaas.inventory.infra.repository;

import com.sportsaas.inventory.domain.entity.Reservation;
import com.sportsaas.inventory.domain.enums.ReservationStatus;
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
 * Repository for Reservation entity.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    Page<Reservation> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Reservation> findByTenantIdAndStatus(UUID tenantId, ReservationStatus status, Pageable pageable);

    List<Reservation> findByOrderId(UUID orderId);

    List<Reservation> findByStockId(UUID stockId);

    List<Reservation> findByStockIdAndStatus(UUID stockId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.status = :status " +
           "AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.tenantId = :tenantId " +
           "AND r.status = 'PENDING' AND r.expiresAt < :now")
    List<Reservation> findExpiredPendingReservations(
            @Param("tenantId") UUID tenantId,
            @Param("now") LocalDateTime now);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Reservation r " +
           "WHERE r.stock.id = :stockId AND r.status = 'PENDING'")
    Integer getTotalPendingQuantity(@Param("stockId") UUID stockId);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, ReservationStatus status);
}
