package lii.authservice.dto;

import lii.authservice.model.User;

public record RegisterRequest(String email, String password, User.Role role) {}
