package lii.restaurantservice.dto;

import java.math.BigDecimal;

public record CreateMenuItemRequest(String name, String description, BigDecimal price) {}
