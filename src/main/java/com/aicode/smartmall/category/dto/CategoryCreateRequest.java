package com.aicode.smartmall.category.dto;

public record CategoryCreateRequest(
        Long parentId,
        String name,
        Integer sortOrder,
        Integer status
) {
}
