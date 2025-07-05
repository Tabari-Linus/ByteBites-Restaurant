package lii.orderservice.dto;

import lii.orderservice.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

public record OrderResponse(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemResponse> items
) {}
