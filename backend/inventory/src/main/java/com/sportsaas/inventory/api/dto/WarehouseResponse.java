package com.sportsaas.inventory.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Warehouse response DTO.
 */
public record WarehouseResponse(
    UUID id,
    String name,
    String address,
    String city,
    String country,
    String postalCode,
    String phone,
    String email,
    Boolean isDefault,
    Boolean active,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
