package com.sportsaas.inventory.api.mapper;

import com.sportsaas.inventory.api.dto.CreateWarehouseRequest;
import com.sportsaas.inventory.api.dto.WarehouseResponse;
import com.sportsaas.inventory.domain.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Warehouse entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WarehouseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Warehouse toEntity(CreateWarehouseRequest request);

    WarehouseResponse toResponse(Warehouse warehouse);
}
