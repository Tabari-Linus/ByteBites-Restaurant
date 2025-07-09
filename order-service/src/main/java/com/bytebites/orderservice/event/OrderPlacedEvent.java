package com.bytebites.orderservice.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("timestamp") LocalDateTime timestamp,
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("customerId") UUID customerId,
        @JsonProperty("customerEmail") String customerEmail,
        @JsonProperty("restaurantId") UUID restaurantId,
        @JsonProperty("restaurantName") String restaurantName,
        @JsonProperty("totalAmount") BigDecimal totalAmount,
        @JsonProperty("items") List<OrderItemInfo> items,
        @JsonProperty("deliveryAddress") String deliveryAddress
) {
    public static OrderPlacedEvent create(
            UUID orderId, UUID customerId, String customerEmail,
            UUID restaurantId, String restaurantName, BigDecimal totalAmount,
            List<OrderItemInfo> items, String deliveryAddress) {
        return new OrderPlacedEvent(
                UUID.randomUUID().toString(),
                "OrderPlaced",
                LocalDateTime.now(),
                orderId, customerId, customerEmail,
                restaurantId, restaurantName, totalAmount,
                items, deliveryAddress
        );
    }
}