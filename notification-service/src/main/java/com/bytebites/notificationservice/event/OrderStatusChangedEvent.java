package com.bytebites.notificationservice.event;

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
        @JsonProperty("previousStatus") String previousStatus,
        @JsonProperty("newStatus") String newStatus,
        @JsonProperty("changedBy") UUID changedBy
) {}
