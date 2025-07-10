package com.bytebites.orderservice.service;

import com.bytebites.orderservice.dto.MenuItemInfo;
import com.bytebites.orderservice.dto.RestaurantInfo;
import com.bytebites.orderservice.exception.RestaurantValidationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "fallbackGetRestaurant")
    @Retry(name = "restaurant-service")
    @TimeLimiter(name = "restaurant-service")
    public CompletableFuture<RestaurantInfo> getRestaurantAsync(UUID restaurantId) {
        logger.info("Fetching restaurant info for ID: {} with circuit breaker", restaurantId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = restaurantServiceUrl + "/api/restaurants/" + restaurantId;
                ResponseEntity<RestaurantInfo> response = restTemplate.getForEntity(url, RestaurantInfo.class);

                if (response.getBody() == null) {
                    throw new RestaurantValidationException("Restaurant not found: " + restaurantId);
                }

                logger.info("Successfully fetched restaurant: {}", response.getBody().name());
                return response.getBody();

            } catch (Exception e) {
                logger.error("Failed to fetch restaurant info for ID: {}, error: {}", restaurantId, e.getMessage());
                throw new RestaurantValidationException("Failed to validate restaurant: " + e.getMessage());
            }
        });
    }

    public RestaurantInfo getRestaurant(UUID restaurantId) {
        try {
            return getRestaurantAsync(restaurantId).get();
        } catch (Exception e) {
            logger.error("Error in synchronous restaurant call: {}", e.getMessage());
            return fallbackGetRestaurant(restaurantId, e);
        }
    }

    @CircuitBreaker(name = "restaurant-menu", fallbackMethod = "fallbackGetMenuItems")
    @Retry(name = "restaurant-menu")
    public List<MenuItemInfo> getMenuItems(UUID restaurantId) {
        logger.info("Fetching menu items for restaurant: {} with circuit breaker", restaurantId);

        try {
            String url = restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu";
            ResponseEntity<List<MenuItemInfo>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MenuItemInfo>>() {}
            );

            List<MenuItemInfo> menuItems = response.getBody();
            logger.info("Successfully fetched {} menu items", menuItems != null ? menuItems.size() : 0);
            return menuItems != null ? menuItems : List.of();

        } catch (Exception e) {
            logger.error("Failed to fetch menu items for restaurant: {}, error: {}", restaurantId, e.getMessage());
            throw new RestaurantValidationException("Failed to validate menu items: " + e.getMessage());
        }
    }

    @CircuitBreaker(name = "restaurant-menu-item", fallbackMethod = "fallbackGetMenuItem")
    @Retry(name = "restaurant-menu-item")
    public MenuItemInfo getMenuItem(UUID restaurantId, UUID menuItemId) {
        logger.info("Fetching menu item: {} from restaurant: {} with circuit breaker", menuItemId, restaurantId);

        try {
            String url = restaurantServiceUrl + "/api/restaurants/" + restaurantId + "/menu/" + menuItemId;
            ResponseEntity<MenuItemInfo> response = restTemplate.getForEntity(url, MenuItemInfo.class);

            if (response.getBody() == null) {
                throw new RestaurantValidationException("Menu item not found: " + menuItemId);
            }

            logger.info("Successfully fetched menu item: {}", response.getBody().name());
            return response.getBody();

        } catch (Exception e) {
            logger.error("Failed to fetch menu item: {} from restaurant: {}, error: {}",
                    menuItemId, restaurantId, e.getMessage());
            throw new RestaurantValidationException("Failed to validate menu item: " + e.getMessage());
        }
    }

    public RestaurantInfo fallbackGetRestaurant(UUID restaurantId, Exception ex) {
        logger.warn("Using fallback for restaurant: {}, reason: {}", restaurantId, ex.getMessage());

        return new RestaurantInfo(
                restaurantId,
                "Not Available, try again later",
                "ACTIVE",
                UUID.randomUUID()
        );
    }

    public List<MenuItemInfo> fallbackGetMenuItems(UUID restaurantId, Exception ex) {
        logger.warn("Using fallback for menu items: {}, reason: {}", restaurantId, ex.getMessage());

        MenuItemInfo fallbackItem = new MenuItemInfo(
                UUID.randomUUID(),
                "Default Item",
                BigDecimal.valueOf(9.99),
                true
        );

        return List.of(fallbackItem);
    }

    public MenuItemInfo fallbackGetMenuItem(UUID restaurantId, UUID menuItemId, Exception ex) {
        logger.warn("Using fallback for menu item: {} from restaurant: {}, reason: {}",
                menuItemId, restaurantId, ex.getMessage());

        return new MenuItemInfo(
                menuItemId,
                "Default Item",
                BigDecimal.valueOf(9.99),
                true
        );
    }
}