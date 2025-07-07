package com.bytebites.restaurantservice.mapper;


import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantResponse;
import com.bytebites.restaurantservice.dto.UpdateRestaurantRequest;
import com.bytebites.restaurantservice.model.Restaurant;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;
@Mapper(componentModel = "spring", uses = {MenuItemMapper.class})
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "status", constant = "PENDING_APPROVAL")
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Restaurant toEntity(CreateRestaurantRequest request, UUID ownerId);

    @Mapping(target = "ownerName", constant = "Restaurant Owner")
    @Mapping(target = "menuItems", source = "menuItems")
    RestaurantResponse toResponse(Restaurant restaurant);

    @Mapping(target = "ownerName", constant = "Restaurant Owner")
    @Mapping(target = "menuItems", ignore = true)
    RestaurantResponse toResponseWithoutMenuItems(Restaurant restaurant);

    @IterableMapping(qualifiedByName = "toResponse")
    List<RestaurantResponse> toResponseList(List<Restaurant> restaurants);

    @Named("toResponse")
    default RestaurantResponse toResponseForList(Restaurant restaurant) {
        return toResponse(restaurant);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateRestaurantRequest request, @MappingTarget Restaurant restaurant);
}