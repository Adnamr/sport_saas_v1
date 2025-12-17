package com.sportsaas.inventory.api.mapper;

import com.sportsaas.inventory.api.dto.StockMovementResponse;
import com.sportsaas.inventory.api.dto.StockResponse;
import com.sportsaas.inventory.domain.entity.Stock;
import com.sportsaas.inventory.domain.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Stock and StockMovement entities.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "availableQuantity", expression = "java(stock.getAvailableQuantity())")
    @Mapping(target = "belowThreshold", expression = "java(stock.isBelowThreshold())")
    StockResponse toResponse(Stock stock);

    @Mapping(target = "stockId", source = "stock.id")
    @Mapping(target = "productId", source = "stock.product.id")
    @Mapping(target = "productName", source = "stock.product.name")
    @Mapping(target = "warehouseId", source = "stock.warehouse.id")
    @Mapping(target = "warehouseName", source = "stock.warehouse.name")
    StockMovementResponse toMovementResponse(StockMovement movement);
}
