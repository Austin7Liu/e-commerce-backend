package com.aicode.smartmall.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        String phone,
        String email,
        Integer status,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}
