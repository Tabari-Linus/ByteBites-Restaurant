package com.bytebites.orderservice.mapper;

import com.bytebites.orderservice.dto.*;
import com.bytebites.orderservice.model.Order;
import com.bytebites.orderservice.model.OrderItem;
import com.bytebites.orderservice.service.RestaurantServiceClient;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class OrderMapper {

    @Autowired
    protected RestaurantServiceClient restaurantServiceClient;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "restaurantId", source = "request.restaurantId")
    @Mapping(target = "restaurantName", expression = "java(getRestaurantName(request.restaurantId()))")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "totalAmount", constant = "0")
    @Mapping(target = "deliveryAddress", source = "request.deliveryAddress")
    @Mapping(target = "customerNotes", source = "request.customerNotes")
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "confirmedAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    public abstract Order toEntity(CreateOrderRequest request, UUID customerId);

    @Mapping(target = "customerName", expression = "java(\"Customer\")")
    @Mapping(target = "items", source = "orderItems")
    public abstract OrderResponse toResponse(Order order);

    @Mapping(target = "customerName", expression = "java(\"Customer\")")
    @Mapping(target = "items", ignore = true)
    public abstract OrderResponse toResponseWithoutItems(Order order);

    public List<OrderResponse> toResponseList(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", source = "order")
    @Mapping(target = "menuItemId", source = "request.menuItemId")
    @Mapping(target = "menuItemName", expression = "java(getMenuItemName(order.getRestaurantId(), request.menuItemId()))")
    @Mapping(target = "unitPrice", expression = "java(getMenuItemPrice(order.getRestaurantId(), request.menuItemId()))")
    @Mapping(target = "quantity", source = "request.quantity")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(getMenuItemPrice(order.getRestaurantId(), request.menuItemId()), request.quantity()))")
    @Mapping(target = "specialInstructions", source = "request.specialInstructions")
    @Mapping(target = "createdAt", ignore = true)
    public abstract OrderItem toOrderItem(OrderItemRequest request, Order order);

    public abstract OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    public abstract List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);

    protected String getRestaurantName(UUID restaurantId) {
        try {
            RestaurantInfo restaurant = restaurantServiceClient.getRestaurant(restaurantId);
            return restaurant.name();
        } catch (Exception e) {
            return "Unknown Restaurant";
        }
    }

    protected String getMenuItemName(UUID restaurantId, UUID menuItemId) {
        try {
            MenuItemInfo menuItem = restaurantServiceClient.getMenuItem(restaurantId, menuItemId);
            return menuItem.name();
        } catch (Exception e) {
            return "Unknown Item";
        }
    }

    protected BigDecimal getMenuItemPrice(UUID restaurantId, UUID menuItemId) {
        try {
            MenuItemInfo menuItem = restaurantServiceClient.getMenuItem(restaurantId, menuItemId);
            return menuItem.price();
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    protected BigDecimal calculateSubtotal(BigDecimal unitPrice, Integer quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}