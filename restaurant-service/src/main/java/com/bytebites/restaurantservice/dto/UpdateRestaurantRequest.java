package com.bytebites.restaurantservice.dto;

import com.bytebites.restaurantservice.enums.RestaurantStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateRestaurantRequest(
        String name,
        String description,
        String address,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
        String phone,

        @Email(message = "Email must be valid")
        String email,

        RestaurantStatus status
) {}