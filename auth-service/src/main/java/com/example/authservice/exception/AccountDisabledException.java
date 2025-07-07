package com.example.authservice.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException(String userAccountIsDisabled) {
        super(userAccountIsDisabled);
    }
}
