package com.example.authservice.dto;

import java.util.Set;
import java.util.UUID;

public record UserInfo(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Set<String> roles
) {}