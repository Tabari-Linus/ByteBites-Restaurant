package com.bytebites.restaurantservice.security;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SecurityService {

    public boolean isOwnerOrAdmin(UUID currentUserId, UUID resourceOwnerId) {
        
        if (currentUserId.equals(resourceOwnerId)) {
            return true;
        }

        
        
        return false;
    }

    public boolean hasRole(List<String> userRoles, String requiredRole) {
        return userRoles != null && userRoles.contains(requiredRole);
    }

    public boolean isAdmin(List<String> userRoles) {
        return hasRole(userRoles, "ROLE_ADMIN");
    }

    public boolean isRestaurantOwner(List<String> userRoles) {
        return hasRole(userRoles, "ROLE_RESTAURANT_OWNER");
    }
}