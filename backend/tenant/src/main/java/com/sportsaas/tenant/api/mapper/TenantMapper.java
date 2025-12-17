package com.sportsaas.tenant.api.mapper;

import com.sportsaas.tenant.api.dto.CreateTenantRequest;
import com.sportsaas.tenant.api.dto.TenantResponse;
import com.sportsaas.tenant.api.dto.UpdateTenantRequest;
import com.sportsaas.tenant.domain.entity.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Tenant entity and DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TenantMapper {

    /**
     * Maps a Tenant entity to a TenantResponse DTO.
     */
    TenantResponse toResponse(Tenant tenant);

    /**
     * Maps a CreateTenantRequest DTO to a Tenant entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "trialEndsAt", ignore = true)
    @Mapping(target = "subscriptionEndsAt", ignore = true)
    @Mapping(target = "maxUsers", ignore = true)
    @Mapping(target = "maxProducts", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tenant toEntity(CreateTenantRequest request);

    /**
     * Updates an existing Tenant entity from an UpdateTenantRequest DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "trialEndsAt", ignore = true)
    @Mapping(target = "subscriptionEndsAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateTenantRequest request, @MappingTarget Tenant tenant);
}
