package com.bytebites.notificationservice.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record RestaurantCreatedEvent(
        @JsonProperty("eventId") String eventId,
        @JsonProperty("eventType") String eventType,
        @JsonProperty("timestamp") LocalDateTime timestamp,
        @JsonProperty("restaurantId") UUID restaurantId,
        @JsonProperty("restaurantName") String restaurantName,
        @JsonProperty("ownerId") UUID ownerId,
        @JsonProperty("ownerEmail") String ownerEmail,
        @JsonProperty("address") String address,
        @JsonProperty("status") String status
) {}
