package com.bytebites.restaurantservice.dto;

public record ErrorResponse(
        String message,
        String status,
        int statusCode
) {
}
