package com.aicode.smartmall.user.service.impl;

import com.aicode.smartmall.user.entity.User;
import com.aicode.smartmall.user.exception.UserConflictException;
import com.aicode.smartmall.user.mapper.UserMapper;
import com.aicode.smartmall.user.service.UserService;
import com.aicode.smartmall.user.service.model.UserPage;
import com.aicode.smartmall.user.service.support.PasswordHasher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;

    public UserServiceImpl(UserMapper userMapper, PasswordHasher passwordHasher) {
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User getById(Long id) {
        validateId(id);
        return userMapper.selectById(id);
    }

    @Override
    public UserPage getPage(int page, int size, Integer status, String keyword) {
        String normalizedKeyword = normalizeAndValidatePageQuery(page, size, status, keyword);
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<User>()
                .eq(status != null, User::getStatus, status)
                .and(normalizedKeyword != null, wrapper -> wrapper
                        .like(User::getUsername, normalizedKeyword)
                        .or()
                        .like(User::getNickname, normalizedKeyword))
                .orderByDesc(User::getId);

        Page<User> result = userMapper.selectPage(new Page<>(page, size), query);
        return new UserPage(
                result.getRecords(),
                result.getTotal(),
                page,
                size,
                result.getPages()
        );
    }

    @Override
    @Transactional
    public User create(User user, String rawPassword) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        user.setUsername(normalizeUsername(user.getUsername()));
        user.setNickname(normalizeNickname(user.getNickname()));
        user.setPhone(normalizePhone(user.getPhone()));
        user.setEmail(normalizeEmail(user.getEmail()));
        validatePassword(rawPassword);
        validateUniqueFields(user, null);

        user.setId(null);
        user.setPasswordHash(passwordHasher.hash(rawPassword));
        user.setStatus(1);
        user.setDeleted(null);
        user.setCreatedTime(null);
        user.setUpdatedTime(null);

        try {
            userMapper.insert(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserConflictException("Username, phone, or email already exists");
        }
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional
    public User updateById(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        validateId(user.getId());
        if (userMapper.selectById(user.getId()) == null) {
            return null;
        }
        if (user.getNickname() == null && user.getPhone() == null
                && user.getEmail() == null && user.getStatus() == null) {
            throw new IllegalArgumentException("At least one user field must be provided for update");
        }

        if (user.getNickname() != null) {
            user.setNickname(normalizeNickname(user.getNickname()));
        }
        if (user.getPhone() != null) {
            user.setPhone(normalizePhone(user.getPhone()));
        }
        if (user.getEmail() != null) {
            user.setEmail(normalizeEmail(user.getEmail()));
        }
        if (user.getStatus() != null) {
            validateStatus(user.getStatus());
        }
        validateUniqueFields(user, user.getId());

        user.setUsername(null);
        user.setPasswordHash(null);
        user.setDeleted(null);
        user.setCreatedTime(null);
        user.setUpdatedTime(null);
        try {
            if (userMapper.updateById(user) == 0) {
                return null;
            }
        } catch (DataIntegrityViolationException exception) {
            throw new UserConflictException("Phone or email already exists");
        }
        return userMapper.selectById(user.getId());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        validateId(id);
        return userMapper.deleteById(id) == 1;
    }

    private void validateUniqueFields(User user, Long excludedId) {
        if (user.getUsername() != null && exists(User::getUsername, user.getUsername(), excludedId)) {
            throw new UserConflictException("Username already exists");
        }
        if (user.getPhone() != null && exists(User::getPhone, user.getPhone(), excludedId)) {
            throw new UserConflictException("Phone already exists");
        }
        if (user.getEmail() != null && exists(User::getEmail, user.getEmail(), excludedId)) {
            throw new UserConflictException("Email already exists");
        }
    }

    private <T> boolean exists(
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<User, T> column,
            T value,
            Long excludedId) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(column, value)
                .ne(excludedId != null, User::getId, excludedId)) > 0;
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User id must be positive");
        }
    }

    private static String normalizeUsername(String username) {
        String normalized = username == null ? null : username.trim();
        if (normalized == null || normalized.length() < 3 || normalized.length() > 50) {
            throw new IllegalArgumentException("Username length must be between 3 and 50 characters");
        }
        return normalized;
    }

    private static String normalizeNickname(String nickname) {
        String normalized = nickname == null ? null : nickname.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("User nickname must not be blank");
        }
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("User nickname must not exceed 100 characters");
        }
        return normalized;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String normalized = phone.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 20) {
            throw new IllegalArgumentException("User phone must not exceed 20 characters");
        }
        return normalized;
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        int atIndex = normalized.indexOf('@');
        if (normalized.length() > 254 || atIndex <= 0
                || atIndex != normalized.lastIndexOf('@')
                || atIndex == normalized.length() - 1) {
            throw new IllegalArgumentException("User email format is invalid");
        }
        return normalized;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 64 || password.isBlank()) {
            throw new IllegalArgumentException("Password length must be between 8 and 64 characters");
        }
    }

    private static void validateStatus(Integer status) {
        if (status != 0 && status != 1) {
            throw new IllegalArgumentException("User status must be 0 or 1");
        }
    }

    private static String normalizeAndValidatePageQuery(
            int page,
            int size,
            Integer status,
            String keyword) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be at least 1");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (status != null) {
            validateStatus(status);
        }
        String normalized = keyword == null ? null : keyword.trim();
        if (normalized != null && normalized.length() > 100) {
            throw new IllegalArgumentException("User keyword must not exceed 100 characters");
        }
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }
}
