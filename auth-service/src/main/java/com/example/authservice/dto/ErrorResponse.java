package com.example.authservice.dto;

public record ErrorResponse(
        int status,
        String message
) {}