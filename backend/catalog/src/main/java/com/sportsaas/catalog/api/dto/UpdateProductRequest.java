package com.sportsaas.catalog.api.dto;

import com.sportsaas.catalog.domain.enums.ProductCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Update product request DTO.
 */
public record UpdateProductRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    String name,

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be at most 255 characters")
    String slug,

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must be at most 100 characters")
    String sku,

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    String description,

    @Size(max = 500, message = "Short description must be at most 500 characters")
    String shortDescription,

    @NotNull(message = "Category ID is required")
    UUID categoryId,

    ProductCondition condition,

    @Positive(message = "Purchase price must be positive")
    BigDecimal purchasePrice,

    @Positive(message = "Rental price daily must be positive")
    BigDecimal rentalPriceDaily,

    @Positive(message = "Rental price weekly must be positive")
    BigDecimal rentalPriceWeekly,

    @Positive(message = "Rental price monthly must be positive")
    BigDecimal rentalPriceMonthly,

    @Positive(message = "Sale price must be positive")
    BigDecimal salePrice,

    @Positive(message = "Deposit amount must be positive")
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
    Integer maxRentalDays
) {}
