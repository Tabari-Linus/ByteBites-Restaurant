package com.example.authservice.exception;

public class TokenExpiredException extends RuntimeException  {
    public TokenExpiredException(String refreshTokenIsExpired) {
        super(refreshTokenIsExpired);
    }
}
