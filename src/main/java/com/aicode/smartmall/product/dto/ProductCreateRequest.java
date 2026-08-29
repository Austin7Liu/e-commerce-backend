package com.aicode.smartmall.product.dto;

import java.math.BigDecimal;

public record ProductCreateRequest(
        String name,
        String mainImageUrl,
        BigDecimal price,
        Long stock,
        String description,
        Integer status
) {
}
