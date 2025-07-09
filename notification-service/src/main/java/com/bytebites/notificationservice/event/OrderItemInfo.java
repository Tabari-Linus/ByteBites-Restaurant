package com.bytebites.notificationservice.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemInfo(
        @JsonProperty("menuItemId") UUID menuItemId,
        @JsonProperty("menuItemName") String menuItemName,
        @JsonProperty("unitPrice") BigDecimal unitPrice,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("subtotal") BigDecimal subtotal
) {}