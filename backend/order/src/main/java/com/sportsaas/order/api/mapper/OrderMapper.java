package com.sportsaas.order.api.mapper;

import com.sportsaas.order.api.dto.CreateOrderRequest;
import com.sportsaas.order.api.dto.OrderResponse;
import com.sportsaas.order.api.dto.UpdateOrderRequest;
import com.sportsaas.order.domain.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for Order entity.
 */
@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "depositReturned", ignore = true)
    @Mapping(target = "confirmedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(CreateOrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paidAmount", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "depositReturned", ignore = true)
    @Mapping(target = "confirmedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateOrderRequest request, @MappingTarget Order order);

    @Mapping(target = "remainingAmount", expression = "java(order.getRemainingAmount())")
    @Mapping(target = "rentalDurationDays", expression = "java(order.getRentalDurationDays())")
    @Mapping(target = "isOverdue", expression = "java(order.isOverdue())")
    OrderResponse toResponse(Order order);
}
