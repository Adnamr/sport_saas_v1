package com.sportsaas.order.api.mapper;

import com.sportsaas.order.api.dto.CreateOrderItemRequest;
import com.sportsaas.order.api.dto.OrderItemResponse;
import com.sportsaas.order.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for OrderItem entity.
 */
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "productSku", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "reservationId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toEntity(CreateOrderItemRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "effectiveUnitPrice", expression = "java(item.getEffectiveUnitPrice())")
    OrderItemResponse toResponse(OrderItem item);
}
