package com.bytebites.orderservice.service;

import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventPublishingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);

    
    public void publishOrderPlacedEvent(Order order) {
        logger.info("Publishing OrderPlacedEvent for order: {}", order.getId());

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                order.getRestaurantName(),
                order.getTotalAmount(),
                mapOrderItems(order),
                order.getCreatedAt()
        );

        
        logger.info("OrderPlacedEvent published: {}", event);
    }

    public void publishOrderStatusChangedEvent(Order order, OrderStatus previousStatus, UUID changedBy) {
        logger.info("Publishing OrderStatusChangedEvent for order: {} from {} to {}",
                order.getId(), previousStatus, order.getStatus());

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                order.getId(),
                previousStatus,
                order.getStatus(),
                changedBy,
                LocalDateTime.now()
        );

        
        logger.info("OrderStatusChangedEvent published: {}", event);
    }

    private List<OrderItemInfo> mapOrderItems(Order order) {
        return order.getOrderItems().stream()
                .map(item -> new OrderItemInfo(
                        item.getMenuItemId(),
                        item.getMenuItemName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();
    }

    
    public record OrderPlacedEvent(
            UUID orderId,
            UUID customerId,
            UUID restaurantId,
            String restaurantName,
            java.math.BigDecimal totalAmount,
            List<OrderItemInfo> items,
            LocalDateTime placedAt
    ) {}

    public record OrderStatusChangedEvent(
            UUID orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            UUID changedBy,
            LocalDateTime changedAt
    ) {}

    public record OrderItemInfo(
            UUID menuItemId,
            String menuItemName,
            java.math.BigDecimal unitPrice,
            Integer quantity,
            java.math.BigDecimal subtotal
    ) {}
}