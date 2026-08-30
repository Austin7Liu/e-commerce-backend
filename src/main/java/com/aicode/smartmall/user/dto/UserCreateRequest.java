package com.aicode.smartmall.user.dto;

public record UserCreateRequest(
        String username,
        String password,
        String nickname,
        String phone,
        String email
) {
}
