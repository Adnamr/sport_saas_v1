package com.sportsaas.catalog.api.mapper;

import com.sportsaas.catalog.api.dto.CreateProductRequest;
import com.sportsaas.catalog.api.dto.ProductAttributeResponse;
import com.sportsaas.catalog.api.dto.ProductImageResponse;
import com.sportsaas.catalog.api.dto.ProductResponse;
import com.sportsaas.catalog.domain.entity.Product;
import com.sportsaas.catalog.domain.entity.ProductAttribute;
import com.sportsaas.catalog.domain.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Product entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "rentalCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "primaryImageUrl", expression = "java(product.getPrimaryImageUrl())")
    ProductResponse toResponse(Product product);

    ProductImageResponse toImageResponse(ProductImage image);

    ProductAttributeResponse toAttributeResponse(ProductAttribute attribute);
}
