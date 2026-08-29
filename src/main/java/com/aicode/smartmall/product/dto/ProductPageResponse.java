package com.aicode.smartmall.product.dto;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> products,
        long total,
        int page,
        int size,
        long totalPages
) {
}
