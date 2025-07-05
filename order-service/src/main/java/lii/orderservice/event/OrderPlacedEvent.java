package lii.orderservice.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID orderId,
        UUID customerId,
        UUID restaurantId,
        BigDecimal totalPrice,
        List<OrderItemData> items
) {
    public record OrderItemData(UUID menuItemId, int quantity) {}
}