package com.example.authservice.exception;

public class RoleNotFoundException extends RuntimeException  {
    public RoleNotFoundException(String defaultRoleNotFound) {
        super(defaultRoleNotFound);
    }
}
