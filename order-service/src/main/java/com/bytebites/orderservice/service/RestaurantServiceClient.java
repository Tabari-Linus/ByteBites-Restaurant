package com.bytebites.orderservice.service;

import com.bytebites.orderservice.dto.MenuItemInfo;
import com.bytebites.orderservice.dto.RestaurantInfo;
import com.bytebites.orderservice.exception.RestaurantValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class RestaurantServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantServiceClient.class);

    private final RestTemplate restTemplate;
    private final String restaurantServiceUrl;

    public RestaurantServiceClient(RestTemplate restTemplate,
                                   @Value("${bytebites.services.restaurant.url}") String restaurantServiceUrl) {
        this.restTemplate = restTemplate;
        this.restaurantServiceUrl = restaurantServiceUrl;
    }

    public RestaurantInfo getRestaurant(UUID restaurantId) {
        logger.info("Fetching restaurant info for ID: {}", restaurantId);

        try {
            String url = restaurantServiceUrl + "/api/restaurants/" + restaurantId;
            ResponseEntity<RestaurantInfo> response = restTemplate.getForEntity(url, RestaurantInfo.class);

            if (response.getBody() == null) {
                throw new RestaurantValidationException("Restaurant not found: " + restaurantId);
            }

            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to fetch restaurant info for ID: {}, error: {}", restaurantId, e.getMessage());
            throw new RestaurantValidationException("Failed to validate restaurant: " + e.getMessage());
        }
    }

    public List<MenuItemInfo> getMenuItems(UUID restaurantId) {
        logger.info("Fetching menu items for restaurant: {}", restaurantId);

        try {
            String url = restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu";
            ResponseEntity<List<MenuItemInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MenuItemInfo>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to fetch menu items for restaurant: {}, error: {}", restaurantId, e.getMessage());
            throw new RestaurantValidationException("Failed to validate menu items: " + e.getMessage());
        }
    }

    public MenuItemInfo getMenuItem(UUID restaurantId, UUID menuItemId) {
        logger.info("Fetching menu item: {} from restaurant: {}", menuItemId, restaurantId);

        try {
            String url = restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu/" + menuItemId;
            ResponseEntity<MenuItemInfo> response = restTemplate.getForEntity(url, MenuItemInfo.class);

            if (response.getBody() == null) {
                throw new RestaurantValidationException("Menu item not found: " + menuItemId);
            }

            return response.getBody();
        } catch (Exception e) {
            logger.error("Failed to fetch menu item: {} from restaurant: {}, error: {}",
                    menuItemId, restaurantId, e.getMessage());
            throw new RestaurantValidationException("Failed to validate menu item: " + e.getMessage());
        }
    }
}