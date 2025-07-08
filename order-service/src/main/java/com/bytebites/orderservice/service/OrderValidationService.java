package com.bytebites.orderservice.service;

import com.bytebites.orderservice.dto.CreateOrderRequest;
import com.bytebites.orderservice.dto.MenuItemInfo;
import com.bytebites.orderservice.dto.OrderItemRequest;
import com.bytebites.orderservice.dto.RestaurantInfo;
import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.exception.InvalidOrderStateException;
import com.bytebites.orderservice.exception.RestaurantValidationException;
import com.bytebites.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderValidationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderValidationService.class);

    private final RestaurantServiceClient restaurantServiceClient;

    public OrderValidationService(RestaurantServiceClient restaurantServiceClient) {
        this.restaurantServiceClient = restaurantServiceClient;
    }

    public boolean validateOrderRequest(CreateOrderRequest request, UUID customerId) {
        logger.info("Validating order request for customer: {} and restaurant: {}",
                customerId, request.restaurantId());

        
        RestaurantInfo restaurant = restaurantServiceClient.getRestaurant(request.restaurantId());
        if (!"ACTIVE".equals(restaurant.status())) {
            throw new RestaurantValidationException("Restaurant is not active: " + restaurant.id());
        }

        
        validateMenuItems(request.restaurantId(), request.items());

        logger.info("Order request validation successful");
        return true;
    }

    private void validateMenuItems(UUID restaurantId, List<OrderItemRequest> items) {
        logger.info("Validating {} menu items for restaurant: {}", items.size(), restaurantId);

        List<UUID> menuItemIds = items.stream()
                .map(OrderItemRequest::menuItemId)
                .toList();

        
        List<MenuItemInfo> menuItems = restaurantServiceClient.getMenuItems(restaurantId);
        Map<UUID, MenuItemInfo> menuItemMap = menuItems.stream()
                .collect(Collectors.toMap(MenuItemInfo::id, Function.identity()));

        
        for (OrderItemRequest item : items) {
            MenuItemInfo menuItem = menuItemMap.get(item.menuItemId());

            if (menuItem == null) {
                throw new RestaurantValidationException(
                        "Menu item not found: " + item.menuItemId()
                );
            }

            if (!menuItem.available()) {
                throw new RestaurantValidationException(
                        "Menu item is not available: " + menuItem.name()
                );
            }
        }

        logger.info("Menu items validation successful");
    }

    public boolean canUpdateOrderStatus(Order order, OrderStatus newStatus, UUID userId) {
        logger.info("Validating status update for order: {} from {} to {} by user: {}",
                order.getId(), order.getStatus(), newStatus, userId);

        if (!order.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException(
                    String.format("Cannot transition from %s to %s", order.getStatus(), newStatus)
            );
        }

        
        

        logger.info("Status update validation successful");
        return true;
    }

    public boolean canAccessOrder(Order order, UUID userId, List<String> userRoles) {
        logger.info("Validating order access for user: {} with roles: {}", userId, userRoles);

        
        if (userRoles.contains("ROLE_ADMIN")) {
            return true;
        }

        
        if (userRoles.contains("ROLE_CUSTOMER") && order.getCustomerId().equals(userId)) {
            return true;
        }

        
        if (userRoles.contains("ROLE_RESTAURANT_OWNER")) {
            try {
                RestaurantInfo restaurant = restaurantServiceClient.getRestaurant(order.getRestaurantId());
                return restaurant.ownerId().equals(userId);
            } catch (Exception e) {
                logger.warn("Failed to validate restaurant ownership: {}", e.getMessage());
                return false;
            }
        }

        logger.warn("User {} does not have access to order: {}", userId, order.getId());
        return false;
    }
}
