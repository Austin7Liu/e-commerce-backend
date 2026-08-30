package com.aicode.smartmall.product.dto;

import java.math.BigDecimal;

public record ProductCreateRequest(
        Long categoryId,
        String name,
        String mainImageUrl,
        BigDecimal price,
        Long stock,
        String description,
        Integer status
) {
}
