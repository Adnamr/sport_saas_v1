package com.sportsaas.catalog.api.dto;

import java.util.UUID;

/**
 * Product attribute response DTO.
 */
public record ProductAttributeResponse(
    UUID id,
    String name,
    String value,
    String attributeType,
    Integer sortOrder,
    Boolean isFilterable,
    Boolean isVisible
) {}
