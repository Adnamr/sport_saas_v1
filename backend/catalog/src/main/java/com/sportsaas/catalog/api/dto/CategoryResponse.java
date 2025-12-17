package com.sportsaas.catalog.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Category response DTO.
 */
public record CategoryResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String imageUrl,
    UUID parentId,
    String parentName,
    Integer sortOrder,
    Boolean active,
    Integer productCount,
    Integer level,
    String fullPath,
    List<CategoryResponse> children,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
