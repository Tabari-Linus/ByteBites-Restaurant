package com.example.authservice.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
}
