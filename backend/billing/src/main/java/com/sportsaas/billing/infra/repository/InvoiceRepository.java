package com.sportsaas.billing.infra.repository;

import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Invoice entity.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Invoice> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Invoice> findByInvoiceNumberAndTenantId(String invoiceNumber, UUID tenantId);

    Optional<Invoice> findFirstByOrderIdAndTenantId(UUID orderId, UUID tenantId);

    Page<Invoice> findByOrderIdAndTenantId(UUID orderId, UUID tenantId, Pageable pageable);

    Page<Invoice> findByTenantIdAndStatus(UUID tenantId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByTenantIdAndCustomerId(UUID tenantId, UUID customerId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.status NOT IN ('PAID', 'CANCELLED') AND i.dueDate < :date")
    List<Invoice> findOverdueInvoices(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.status NOT IN ('PAID', 'CANCELLED') AND i.dueDate < :date")
    Page<Invoice> findOverdueInvoicesPaged(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.status NOT IN ('PAID', 'CANCELLED') AND i.dueDate = :date")
    List<Invoice> findInvoicesDueOn(@Param("tenantId") UUID tenantId, @Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId AND i.issueDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByTenantIdAndDateRange(
        @Param("tenantId") UUID tenantId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, InvoiceStatus status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status NOT IN ('PAID', 'CANCELLED') AND i.dueDate < CURRENT_DATE")
    long countOverdueInvoices(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status = 'PAID'")
    java.math.BigDecimal sumPaidInvoicesTotal(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(i.totalAmount - i.paidAmount), 0) FROM Invoice i WHERE i.tenantId = :tenantId AND i.status NOT IN ('PAID', 'CANCELLED')")
    java.math.BigDecimal sumOutstandingAmount(@Param("tenantId") UUID tenantId);

    @Query("SELECT MAX(i.invoiceNumber) FROM Invoice i WHERE i.tenantId = :tenantId")
    String findLastInvoiceNumber(@Param("tenantId") UUID tenantId);

    boolean existsByInvoiceNumberAndTenantId(String invoiceNumber, UUID tenantId);
}
