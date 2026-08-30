package com.aicode.smartmall.category.dto;

public record CategoryListQueryRequest(
        Integer page,
        Integer size,
        Long parentId,
        Integer status,
        String name
) {
}
