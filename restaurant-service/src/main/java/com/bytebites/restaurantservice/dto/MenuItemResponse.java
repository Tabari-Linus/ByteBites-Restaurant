package com.bytebites.restaurantservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        String category,
        Boolean available,
        String imageUrl,
        LocalDateTime createdAt
) {}