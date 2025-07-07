package com.bytebites.restaurantservice.mapper;

import com.bytebites.restaurantservice.dto.CreateMenuItemRequest;
import com.bytebites.restaurantservice.dto.MenuItemResponse;
import com.bytebites.restaurantservice.dto.UpdateMenuItemRequest;
import com.bytebites.restaurantservice.model.MenuItem;
import com.bytebites.restaurantservice.model.Restaurant;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", source = "restaurant")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MenuItem toEntity(CreateMenuItemRequest request, Restaurant restaurant);

    MenuItemResponse toResponse(MenuItem menuItem);

    List<MenuItemResponse> toResponseList(List<MenuItem> menuItems);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateMenuItemRequest request, @MappingTarget MenuItem menuItem);
}