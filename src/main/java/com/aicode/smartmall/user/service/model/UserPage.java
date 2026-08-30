package com.aicode.smartmall.user.service.model;

import com.aicode.smartmall.user.entity.User;

import java.util.List;

public record UserPage(
        List<User> users,
        long total,
        int page,
        int size,
        long totalPages
) {
}
