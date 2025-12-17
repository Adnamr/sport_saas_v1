package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Update warehouse request DTO.
 */
public record UpdateWarehouseRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    String name,

    @Size(max = 500, message = "Address must be at most 500 characters")
    String address,

    @Size(max = 100, message = "City must be at most 100 characters")
    String city,

    @Size(max = 100, message = "Country must be at most 100 characters")
    String country,

    @Size(max = 20, message = "Postal code must be at most 20 characters")
    String postalCode,

    @Size(max = 50, message = "Phone must be at most 50 characters")
    String phone,

    @Size(max = 255, message = "Email must be at most 255 characters")
    String email,

    @Size(max = 1000, message = "Notes must be at most 1000 characters")
    String notes
) {}
