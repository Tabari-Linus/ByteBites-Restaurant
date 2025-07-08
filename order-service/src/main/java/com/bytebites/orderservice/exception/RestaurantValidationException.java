package com.bytebites.orderservice.exception;

public class RestaurantValidationException extends RuntimeException {
    public RestaurantValidationException(String message) {
        super(message);
    }
}
