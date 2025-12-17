package com.sportsaas.billing.infra.repository;

import com.sportsaas.billing.domain.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for InvoiceItem entity.
 */
@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {

    List<InvoiceItem> findByInvoiceId(UUID invoiceId);

    Optional<InvoiceItem> findByIdAndTenantId(UUID id, UUID tenantId);

    void deleteByInvoiceId(UUID invoiceId);
}
