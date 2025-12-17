package com.sportsaas.inventory.infra.service;

import com.sportsaas.common.exception.BusinessException;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.domain.entity.Reservation;
import com.sportsaas.inventory.domain.entity.Stock;
import com.sportsaas.inventory.domain.enums.ReservationStatus;
import com.sportsaas.inventory.domain.service.ReservationService;
import com.sportsaas.inventory.domain.service.StockService;
import com.sportsaas.inventory.infra.repository.ReservationRepository;
import com.sportsaas.inventory.infra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ReservationService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;

    @Override
    @Transactional
    public Reservation create(UUID stockId, int quantity, UUID orderId, int expirationMinutes) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }

        Stock stock = stockRepository.findById(stockId)
            .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        if (!stock.hasAvailable(quantity)) {
            throw new BusinessException("Insufficient available stock. Available: " + stock.getAvailableQuantity());
        }

        // Reserve the stock
        stockService.reserveStock(stockId, quantity, orderId, null);

        // Create reservation
        Reservation reservation = Reservation.builder()
            .stock(stock)
            .tenantId(stock.getTenantId())
            .orderId(orderId)
            .quantity(quantity)
            .status(ReservationStatus.PENDING)
            .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
            .build();

        log.info("Created reservation for {} units of stock {} for order {}",
                 quantity, stockId, orderId);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public Reservation confirm(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        if (!reservation.canConfirm()) {
            throw new BusinessException("Reservation cannot be confirmed. Status: " + reservation.getStatus());
        }

        if (reservation.isExpired()) {
            throw new BusinessException("Reservation has expired");
        }

        // Remove stock permanently (it's been rented/sold)
        Stock stock = reservation.getStock();
        stock.setQuantity(stock.getQuantity() - reservation.getQuantity());
        stock.setReservedQuantity(stock.getReservedQuantity() - reservation.getQuantity());
        stockRepository.save(stock);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());

        log.info("Confirmed reservation {}", reservationId);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public Reservation release(UUID reservationId, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        if (!reservation.canRelease()) {
            throw new BusinessException("Reservation cannot be released. Status: " + reservation.getStatus());
        }

        // Release the reserved stock
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            stockService.releaseStock(
                reservation.getStock().getId(),
                reservation.getQuantity(),
                reservation.getOrderId(),
                null
            );
        }

        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setReleasedAt(LocalDateTime.now());
        reservation.setNotes(reason);

        log.info("Released reservation {} - reason: {}", reservationId, reason);
        return reservationRepository.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservationRepository.findById(id);
    }

    @Override
    public Page<Reservation> findByTenantId(UUID tenantId, Pageable pageable) {
        return reservationRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Page<Reservation> findByStatus(UUID tenantId, ReservationStatus status, Pageable pageable) {
        return reservationRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    public List<Reservation> findByOrderId(UUID orderId) {
        return reservationRepository.findByOrderId(orderId);
    }

    @Override
    public List<Reservation> findByStockId(UUID stockId) {
        return reservationRepository.findByStockId(stockId);
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 60000) // Run every minute
    public void expireReservations() {
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(
            ReservationStatus.PENDING,
            LocalDateTime.now()
        );

        for (Reservation reservation : expiredReservations) {
            try {
                // Release the reserved stock
                stockService.releaseStock(
                    reservation.getStock().getId(),
                    reservation.getQuantity(),
                    reservation.getOrderId(),
                    null
                );

                reservation.setStatus(ReservationStatus.EXPIRED);
                reservation.setReleasedAt(LocalDateTime.now());
                reservation.setNotes("Automatically expired");
                reservationRepository.save(reservation);

                log.info("Expired reservation {}", reservation.getId());
            } catch (Exception e) {
                log.error("Failed to expire reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }

        if (!expiredReservations.isEmpty()) {
            log.info("Expired {} reservations", expiredReservations.size());
        }
    }

    @Override
    @Transactional
    public void expireReservationsForTenant(UUID tenantId) {
        List<Reservation> expiredReservations = reservationRepository.findExpiredPendingReservations(
            tenantId,
            LocalDateTime.now()
        );

        for (Reservation reservation : expiredReservations) {
            try {
                stockService.releaseStock(
                    reservation.getStock().getId(),
                    reservation.getQuantity(),
                    reservation.getOrderId(),
                    null
                );

                reservation.setStatus(ReservationStatus.EXPIRED);
                reservation.setReleasedAt(LocalDateTime.now());
                reservation.setNotes("Automatically expired");
                reservationRepository.save(reservation);
            } catch (Exception e) {
                log.error("Failed to expire reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }
    }

    @Override
    public long countByTenantId(UUID tenantId) {
        return reservationRepository.countByTenantId(tenantId);
    }

    @Override
    public long countByStatus(UUID tenantId, ReservationStatus status) {
        return reservationRepository.countByTenantIdAndStatus(tenantId, status);
    }
}
