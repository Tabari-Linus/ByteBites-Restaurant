package com.example.authservice.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserAlreadyExistsException extends RuntimeException  {
    public UserAlreadyExistsException(@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String s) {
        super(s);
    }
}
