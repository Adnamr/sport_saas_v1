package com.sportsaas.inventory.domain;

import com.sportsaas.inventory.domain.entity.Reservation;
import com.sportsaas.inventory.domain.enums.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Reservation entity.
 */
class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = Reservation.builder()
            .tenantId(UUID.randomUUID())
            .quantity(5)
            .status(ReservationStatus.PENDING)
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();
    }

    @Test
    void shouldNotBeExpiredWhenInFuture() {
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        assertThat(reservation.isExpired()).isFalse();
    }

    @Test
    void shouldBeExpiredWhenInPast() {
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(reservation.isExpired()).isTrue();
    }

    @Test
    void shouldNotBeExpiredWhenConfirmed() {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(reservation.isExpired()).isFalse();
    }

    @Test
    void shouldCanConfirmWhenPendingAndNotExpired() {
        assertThat(reservation.canConfirm()).isTrue();
    }

    @Test
    void shouldNotCanConfirmWhenExpired() {
        reservation.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThat(reservation.canConfirm()).isFalse();
    }

    @Test
    void shouldNotCanConfirmWhenAlreadyConfirmed() {
        reservation.setStatus(ReservationStatus.CONFIRMED);

        assertThat(reservation.canConfirm()).isFalse();
    }

    @Test
    void shouldCanReleaseWhenPending() {
        assertThat(reservation.canRelease()).isTrue();
    }

    @Test
    void shouldCanReleaseWhenConfirmed() {
        reservation.setStatus(ReservationStatus.CONFIRMED);

        assertThat(reservation.canRelease()).isTrue();
    }

    @Test
    void shouldNotCanReleaseWhenAlreadyReleased() {
        reservation.setStatus(ReservationStatus.RELEASED);

        assertThat(reservation.canRelease()).isFalse();
    }

    @Test
    void shouldNotCanReleaseWhenExpired() {
        reservation.setStatus(ReservationStatus.EXPIRED);

        assertThat(reservation.canRelease()).isFalse();
    }

    @Test
    void shouldHaveDefaultStatusPending() {
        Reservation newReservation = Reservation.builder()
            .tenantId(UUID.randomUUID())
            .quantity(1)
            .build();

        assertThat(newReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }
}
