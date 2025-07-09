package com.bytebites.orderservice.event;

import com.bytebites.orderservice.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusChangedEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("timestamp") LocalDateTime timestamp,
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("customerId") UUID customerId,
        @JsonProperty("customerEmail") String customerEmail,
        @JsonProperty("restaurantId") UUID restaurantId,
        @JsonProperty("restaurantName") String restaurantName,
        @JsonProperty("previousStatus") OrderStatus previousStatus,
        @JsonProperty("newStatus") OrderStatus newStatus,
        @JsonProperty("changedBy") UUID changedBy
) {
    public static OrderStatusChangedEvent create(
            UUID orderId, UUID customerId, String customerEmail,
            UUID restaurantId, String restaurantName,
            OrderStatus previousStatus, OrderStatus newStatus, UUID changedBy) {
        return new OrderStatusChangedEvent(
                UUID.randomUUID().toString(),
                "OrderStatusChanged",
                LocalDateTime.now(),
                orderId, customerId, customerEmail,
                restaurantId, restaurantName,
                previousStatus, newStatus, changedBy
        );
    }
}