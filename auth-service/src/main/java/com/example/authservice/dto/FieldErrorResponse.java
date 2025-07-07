package com.example.authservice.dto;

import java.util.List;

public record FieldErrorResponse(
        int status,
        String message,
        List<FieldValidationError> errors
) {
}
