package com.bytebites.restaurantservice.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateMenuItemRequest(
        String name,
        String description,

        @DecimalMin(value = "0.0", message = "Price must be positive")
        BigDecimal price,

        String category,
        Boolean available,
        String imageUrl
) {}