package com.aicode.smartmall.user.dto;

import java.util.List;

public record UserPageResponse(
        List<UserResponse> users,
        long total,
        int page,
        int size,
        long totalPages
) {
}
