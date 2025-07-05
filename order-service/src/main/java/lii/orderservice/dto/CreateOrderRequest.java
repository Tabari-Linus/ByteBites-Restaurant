package lii.orderservice.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID customerId,
        UUID restaurantId,
        List<OrderItemRequest> items
) {}
