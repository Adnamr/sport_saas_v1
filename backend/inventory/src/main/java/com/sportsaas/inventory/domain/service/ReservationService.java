package com.sportsaas.inventory.domain.service;

import com.sportsaas.inventory.domain.entity.Reservation;
import com.sportsaas.inventory.domain.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for Reservation operations.
 */
public interface ReservationService {

    Reservation create(UUID stockId, int quantity, UUID orderId, int expirationMinutes);

    Reservation confirm(UUID reservationId);

    Reservation release(UUID reservationId, String reason);

    Optional<Reservation> findById(UUID id);

    Page<Reservation> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Reservation> findByStatus(UUID tenantId, ReservationStatus status, Pageable pageable);

    List<Reservation> findByOrderId(UUID orderId);

    List<Reservation> findByStockId(UUID stockId);

    void expireReservations();

    void expireReservationsForTenant(UUID tenantId);

    long countByTenantId(UUID tenantId);

    long countByStatus(UUID tenantId, ReservationStatus status);
}
