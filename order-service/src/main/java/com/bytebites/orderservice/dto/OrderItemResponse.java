package com.bytebites.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID menuItemId,
        String menuItemName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal,
        String specialInstructions
) {}