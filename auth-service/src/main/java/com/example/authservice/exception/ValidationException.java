package com.example.authservice.exception;

public class ValidationException extends RuntimeException  {
    public ValidationException(String s) {
        super(s);
    }
}
