package lii.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
        UUID menuItemId,
        Integer quantity,
        BigDecimal price
) {}
