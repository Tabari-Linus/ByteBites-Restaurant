package com.bytebites.orderservice.dto;

import java.util.UUID;

public record RestaurantInfo(
        UUID id,
        String name,
        String status,
        UUID ownerId
) {}