package com.sportsaas.billing.api.mapper;

import com.sportsaas.billing.api.dto.CreatePaymentRequest;
import com.sportsaas.billing.api.dto.PaymentResponse;
import com.sportsaas.billing.domain.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Payment entity.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentNumber", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "isRefund", ignore = true)
    @Mapping(target = "refundOfPaymentId", ignore = true)
    Payment toEntity(CreatePaymentRequest request);

    @Mapping(target = "invoiceId", source = "invoice.id")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
