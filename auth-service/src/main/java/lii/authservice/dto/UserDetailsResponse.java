package lii.authservice.dto;

import lii.authservice.model.User;

import java.util.List;

public record UserDetailsResponse(
    String userName,
    List<User.Role> role,
    String userId
) {
}
