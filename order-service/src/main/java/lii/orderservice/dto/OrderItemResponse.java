package lii.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID orderItemId,
        Integer quantity,
        BigDecimal price
) {
}
