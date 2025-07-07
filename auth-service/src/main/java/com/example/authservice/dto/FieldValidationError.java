package com.example.authservice.dto;

public record FieldValidationError(
        String field,
        String error
) {
}
