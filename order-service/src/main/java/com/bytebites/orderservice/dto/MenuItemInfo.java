package com.bytebites.orderservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemInfo(
        UUID id,
        String name,
        BigDecimal price,
        Boolean available
) {}