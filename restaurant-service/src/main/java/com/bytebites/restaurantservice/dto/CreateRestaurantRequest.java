package com.bytebites.restaurantservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateRestaurantRequest(
        @NotBlank(message = "Restaurant name is required")
        String name,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
        String phone,

        @Email(message = "Email must be valid")
        String email
) {}
