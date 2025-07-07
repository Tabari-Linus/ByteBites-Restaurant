package com.bytebites.restaurantservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateMenuItemRequest(
        @NotBlank(message = "Menu item name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must be positive")
        BigDecimal price,

        @NotBlank(message = "Category is required")
        String category,

        String imageUrl
) {}