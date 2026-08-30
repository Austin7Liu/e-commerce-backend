package com.aicode.smartmall.product.dto;

public record ProductListQueryRequest(
        Integer page,
        Integer size,
        Integer status,
        String keyword
) {
}
