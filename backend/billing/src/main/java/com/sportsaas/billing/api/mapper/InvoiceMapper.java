package com.sportsaas.billing.api.mapper;

import com.sportsaas.billing.api.dto.CreateInvoiceItemRequest;
import com.sportsaas.billing.api.dto.CreateInvoiceRequest;
import com.sportsaas.billing.api.dto.InvoiceItemResponse;
import com.sportsaas.billing.api.dto.InvoiceResponse;
import com.sportsaas.billing.domain.entity.Invoice;
import com.sportsaas.billing.domain.entity.InvoiceItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.util.List;

/**
 * MapStruct mapper for Invoice and InvoiceItem entities.
 */
@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payments", ignore = true)
    Invoice toEntity(CreateInvoiceRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invoiceNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateEntity(CreateInvoiceRequest request, @MappingTarget Invoice invoice);

    @Mapping(target = "remainingAmount", expression = "java(invoice.getRemainingAmount())")
    @Mapping(target = "isOverdue", expression = "java(invoice.isOverdue())")
    @Mapping(target = "daysUntilDue", expression = "java(calculateDaysUntilDue(invoice))")
    InvoiceResponse toResponse(Invoice invoice);

    List<InvoiceResponse> toResponseList(List<Invoice> invoices);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    InvoiceItem toItemEntity(CreateInvoiceItemRequest request);

    InvoiceItemResponse toItemResponse(InvoiceItem item);

    List<InvoiceItemResponse> toItemResponseList(List<InvoiceItem> items);

    default Integer calculateDaysUntilDue(Invoice invoice) {
        if (invoice.getDueDate() == null) {
            return null;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), invoice.getDueDate());
        return (int) days;
    }
}
