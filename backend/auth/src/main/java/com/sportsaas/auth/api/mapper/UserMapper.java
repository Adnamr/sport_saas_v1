package com.sportsaas.auth.api.mapper;

import com.sportsaas.auth.api.dto.UserResponse;
import com.sportsaas.auth.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for User entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);
}
