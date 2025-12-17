package com.sportsaas.catalog.api.mapper;

import com.sportsaas.catalog.api.dto.CategoryResponse;
import com.sportsaas.catalog.api.dto.CreateCategoryRequest;
import com.sportsaas.catalog.domain.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Category entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "level", expression = "java(category.getLevel())")
    @Mapping(target = "fullPath", expression = "java(category.getFullPath())")
    @Mapping(target = "children", source = "children", qualifiedByName = "mapChildren")
    CategoryResponse toResponse(Category category);

    @Named("mapChildren")
    default List<CategoryResponse> mapChildren(List<Category> children) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        return children.stream()
            .map(this::toResponse)
            .toList();
    }
}
