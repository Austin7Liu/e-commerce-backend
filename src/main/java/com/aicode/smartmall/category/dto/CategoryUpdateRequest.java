package com.aicode.smartmall.category.dto;

public record CategoryUpdateRequest(
        Long parentId,
        String name,
        Integer sortOrder,
        Integer status
) {
}
