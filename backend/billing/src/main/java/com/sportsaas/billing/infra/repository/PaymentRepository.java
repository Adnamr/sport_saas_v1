package com.sportsaas.billing.infra.repository;

import com.sportsaas.billing.domain.entity.Payment;
import com.sportsaas.billing.domain.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Payment> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Payment> findByPaymentNumberAndTenantId(String paymentNumber, UUID tenantId);

    List<Payment> findByInvoiceId(UUID invoiceId);

    Page<Payment> findByInvoiceIdAndTenantId(UUID invoiceId, UUID tenantId, Pageable pageable);

    List<Payment> findByOrderIdAndTenantId(UUID orderId, UUID tenantId);

    Page<Payment> findByOrderIdAndTenantId(UUID orderId, UUID tenantId, Pageable pageable);

    Page<Payment> findByTenantIdAndMethod(UUID tenantId, PaymentMethod method, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByTenantIdAndDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId AND p.isRefund = false")
    Page<Payment> findNonRefundPayments(@Param("tenantId") UUID tenantId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId AND p.isRefund = true")
    Page<Payment> findRefundPayments(@Param("tenantId") UUID tenantId, Pageable pageable);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndMethod(UUID tenantId, PaymentMethod method);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenantId = :tenantId AND p.isRefund = false")
    java.math.BigDecimal sumPaymentsTotal(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.tenantId = :tenantId AND p.isRefund = true")
    java.math.BigDecimal sumRefundsTotal(@Param("tenantId") UUID tenantId);

    @Query("SELECT MAX(p.paymentNumber) FROM Payment p WHERE p.tenantId = :tenantId")
    String findLastPaymentNumber(@Param("tenantId") UUID tenantId);

    boolean existsByPaymentNumberAndTenantId(String paymentNumber, UUID tenantId);
}
