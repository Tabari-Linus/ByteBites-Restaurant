package com.bytebites.restaurantservice.dto;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {
    public T getData(){
        return data;
    }

    public boolean isSuccess(){
        return success;
    }
}
