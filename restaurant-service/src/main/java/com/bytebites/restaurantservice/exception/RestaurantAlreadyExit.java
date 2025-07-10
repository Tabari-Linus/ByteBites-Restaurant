package com.bytebites.restaurantservice.exception;

public class RestaurantAlreadyExit extends RuntimeException {

    public RestaurantAlreadyExit(String message) {
        super(message);
    }
}
