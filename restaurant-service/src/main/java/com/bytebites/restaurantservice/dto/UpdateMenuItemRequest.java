package com.bytebites.restaurantservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateMenuItemRequest(
        String name,
        String description,

        @DecimalMin(value = "0.0", message = "Price must be positive")
        BigDecimal price,

        String category,
        Boolean available,
        @Pattern(regexp = "^(http|https)://.*$", message = "Image URL must be valid")
        String imageUrl
) {}