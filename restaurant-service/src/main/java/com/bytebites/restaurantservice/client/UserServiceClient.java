package com.bytebites.restaurantservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "auth-service")
public interface UserServiceClient {
    @GetMapping("/auth/users/{userId}/roles")
    List<String> getUserRoles(@PathVariable("userId") UUID userId);
}