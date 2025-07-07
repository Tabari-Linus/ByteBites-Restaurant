package com.bytebites.restaurantservice.dto;

import com.bytebites.restaurantservice.enums.RestaurantStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String description,
        String address,
        String phone,
        String email,
        RestaurantStatus status,
        String ownerName,
        LocalDateTime createdAt,
        List<MenuItemResponse> menuItems
) {}