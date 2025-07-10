package com.bytebites.restaurantservice.security;

import com.bytebites.restaurantservice.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class SecurityService {

    private final UserServiceClient userServiceClient;

    @Autowired
    public SecurityService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public boolean isOwnerOrAdmin(UUID currentUserId, UUID resourceOwnerId) {
        if (currentUserId.equals(resourceOwnerId)) {
            return true;
        }
        return isAdmin(currentUserId);
    }

    public boolean hasRole(List<String> userRoles, String requiredRole) {
        return userRoles != null && userRoles.contains(requiredRole);
    }

    public boolean isAdmin(UUID userId) {
        List<String> userRoles = userServiceClient.getUserRoles(userId);
        return hasRole(userRoles, "ROLE_ADMIN");
    }

    public boolean isRestaurantOwner(List<String> userRoles) {
        return hasRole(userRoles, "ROLE_RESTAURANT_OWNER");
    }
}