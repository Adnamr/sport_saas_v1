package com.sportsaas.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Update category request DTO.
 */
public record UpdateCategoryRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    String name,

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be at most 255 characters")
    String slug,

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description,

    String imageUrl,

    UUID parentId,

    Integer sortOrder
) {}
