package com.sportsaas.catalog.api.dto;

import com.sportsaas.catalog.domain.enums.ProductCondition;
import com.sportsaas.catalog.domain.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Product response DTO.
 */
public record ProductResponse(
    UUID id,
    String name,
    String slug,
    String sku,
    String description,
    String shortDescription,
    UUID categoryId,
    String categoryName,
    ProductStatus status,
    ProductCondition condition,
    BigDecimal purchasePrice,
    BigDecimal rentalPriceDaily,
    BigDecimal rentalPriceWeekly,
    BigDecimal rentalPriceMonthly,
    BigDecimal salePrice,
    BigDecimal depositAmount,
    String brand,
    String model,
    String serialNumber,
    String size,
    String color,
    BigDecimal weight,
    String dimensions,
    Boolean isRentable,
    Boolean isSellable,
    Boolean requiresDeposit,
    Integer minRentalDays,
    Integer maxRentalDays,
    String primaryImageUrl,
    List<ProductImageResponse> images,
    List<ProductAttributeResponse> attributes,
    Integer viewCount,
    Integer rentalCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
