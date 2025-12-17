package com.sportsaas.inventory.api.mapper;

import com.sportsaas.inventory.api.dto.ReservationResponse;
import com.sportsaas.inventory.domain.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Reservation entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping(target = "stockId", source = "stock.id")
    @Mapping(target = "productId", source = "stock.product.id")
    @Mapping(target = "productName", source = "stock.product.name")
    @Mapping(target = "warehouseId", source = "stock.warehouse.id")
    @Mapping(target = "warehouseName", source = "stock.warehouse.name")
    ReservationResponse toResponse(Reservation reservation);
}
