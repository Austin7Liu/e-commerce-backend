package com.aicode.smartmall.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String mainImageUrl,
        BigDecimal price,
        Long stock,
        String description,
        Integer status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
