package com.aicode.smartmall.category.dto;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        Long parentId,
        String name,
        Integer sortOrder,
        Integer status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
