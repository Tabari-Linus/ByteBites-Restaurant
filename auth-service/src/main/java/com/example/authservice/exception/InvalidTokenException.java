package com.example.authservice.exception;

public class InvalidTokenException extends RuntimeException  {
    public InvalidTokenException(String invalidRefreshToken) {
        super(invalidRefreshToken);
    }
}
