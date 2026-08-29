package com.aicode.smartmall.product.service.model;

import com.aicode.smartmall.product.entity.Product;

import java.util.List;

public record ProductPage(
        List<Product> products,
        long total,
        int page,
        int size,
        long totalPages
) {
}
