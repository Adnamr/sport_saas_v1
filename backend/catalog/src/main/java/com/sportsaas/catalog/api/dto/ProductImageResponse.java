package com.sportsaas.catalog.api.dto;

import java.util.UUID;

/**
 * Product image response DTO.
 */
public record ProductImageResponse(
    UUID id,
    String url,
    String thumbnailUrl,
    String altText,
    Integer sortOrder,
    Boolean isPrimary
) {}
