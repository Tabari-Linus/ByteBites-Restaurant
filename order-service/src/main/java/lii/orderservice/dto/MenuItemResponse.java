package lii.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemResponse(UUID id, String name, BigDecimal price) {}