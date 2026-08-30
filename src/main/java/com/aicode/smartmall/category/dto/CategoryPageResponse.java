package com.aicode.smartmall.category.dto;

import java.util.List;

public record CategoryPageResponse(
        List<CategoryResponse> categories,
        long total,
        int page,
        int size,
        long totalPages
) {
}
