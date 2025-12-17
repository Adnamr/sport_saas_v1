package com.sportsaas.inventory.api.controller;

import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.api.dto.CreateReservationRequest;
import com.sportsaas.inventory.api.dto.ReservationResponse;
import com.sportsaas.inventory.api.mapper.ReservationMapper;
import com.sportsaas.inventory.domain.entity.Reservation;
import com.sportsaas.inventory.domain.enums.ReservationStatus;
import com.sportsaas.inventory.domain.service.ReservationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reservation REST controller.
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Stock reservation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    @GetMapping
    @Operation(summary = "List reservations", description = "Get all reservations for the current tenant")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ReservationResponse>> list(
            @RequestParam(required = false) ReservationStatus status,
            Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        Page<Reservation> page;

        if (status != null) {
            page = reservationService.findByStatus(tenantId, status, pageable);
        } else {
            page = reservationService.findByTenantId(tenantId, pageable);
        }

        Page<ReservationResponse> responsePage = page.map(reservationMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation", description = "Get a reservation by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> getById(@PathVariable UUID id) {
        Reservation reservation = reservationService.findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation not found: " + id));
        return ResponseEntity.ok(reservationMapper.toResponse(reservation));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get reservations by order", description = "Get all reservations for an order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getByOrderId(@PathVariable UUID orderId) {
        List<Reservation> reservations = reservationService.findByOrderId(orderId);
        List<ReservationResponse> response = reservations.stream()
            .map(reservationMapper::toResponse)
            .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create reservation", description = "Create a new stock reservation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        int expirationMinutes = request.expirationMinutes() != null ? request.expirationMinutes() : 30;

        Reservation reservation = reservationService.create(
            request.stockId(),
            request.quantity(),
            request.orderId(),
            expirationMinutes
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toResponse(reservation));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm reservation", description = "Confirm a pending reservation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ReservationResponse> confirm(@PathVariable UUID id) {
        Reservation reservation = reservationService.confirm(id);
        return ResponseEntity.ok(reservationMapper.toResponse(reservation));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release reservation", description = "Release/cancel a reservation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ReservationResponse> release(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason) {
        Reservation reservation = reservationService.release(id, reason);
        return ResponseEntity.ok(reservationMapper.toResponse(reservation));
    }

    @PostMapping("/expire")
    @Operation(summary = "Expire reservations", description = "Manually trigger expiration of pending reservations")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Map<String, String>> expireReservations() {
        UUID tenantId = TenantContext.requireTenantId();
        reservationService.expireReservationsForTenant(tenantId);
        return ResponseEntity.ok(Map.of("message", "Expired reservations processed"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get reservation stats", description = "Get reservation statistics")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getStats() {
        UUID tenantId = TenantContext.requireTenantId();

        return ResponseEntity.ok(Map.of(
            "total", reservationService.countByTenantId(tenantId),
            "pending", reservationService.countByStatus(tenantId, ReservationStatus.PENDING),
            "confirmed", reservationService.countByStatus(tenantId, ReservationStatus.CONFIRMED),
            "released", reservationService.countByStatus(tenantId, ReservationStatus.RELEASED),
            "expired", reservationService.countByStatus(tenantId, ReservationStatus.EXPIRED)
        ));
    }
}
