package com.bytebites.orderservice.dto;

import com.bytebites.orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String customerName,
        UUID restaurantId,
        String restaurantName,
        OrderStatus status,
        BigDecimal totalAmount,
        String deliveryAddress,
        String customerNotes,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt,
        LocalDateTime deliveredAt
) {}