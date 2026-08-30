package com.aicode.smartmall.user.dto;

public record UserUpdateRequest(
        String nickname,
        String phone,
        String email,
        Integer status
) {
}
