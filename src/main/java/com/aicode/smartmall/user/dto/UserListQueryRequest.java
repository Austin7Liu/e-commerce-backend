package com.aicode.smartmall.user.dto;

public record UserListQueryRequest(
        Integer page,
        Integer size,
        Integer status,
        String keyword
) {
}
