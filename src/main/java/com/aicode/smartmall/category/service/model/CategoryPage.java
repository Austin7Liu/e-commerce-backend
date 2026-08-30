package com.aicode.smartmall.category.service.model;

import com.aicode.smartmall.category.entity.Category;

import java.util.List;

public record CategoryPage(
        List<Category> categories,
        long total,
        int page,
        int size,
        long totalPages
) {
}
