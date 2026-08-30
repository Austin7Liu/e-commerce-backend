package com.aicode.smartmall.user.controller;

import com.aicode.smartmall.user.dto.UserCreateRequest;
import com.aicode.smartmall.user.dto.UserListQueryRequest;
import com.aicode.smartmall.user.dto.UserPageResponse;
import com.aicode.smartmall.user.dto.UserResponse;
import com.aicode.smartmall.user.dto.UserUpdateRequest;
import com.aicode.smartmall.user.entity.User;
import com.aicode.smartmall.user.exception.UserConflictException;
import com.aicode.smartmall.user.service.UserService;
import com.aicode.smartmall.user.service.model.UserPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return user == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(toResponse(user));
    }

    @GetMapping
    public ResponseEntity<UserPageResponse> getPage(@ModelAttribute UserListQueryRequest request) {
        int page = request.page() == null ? 1 : request.page();
        int size = request.size() == null ? 20 : request.size();
        UserPage userPage = userService.getPage(page, size, request.status(), request.keyword());
        return ResponseEntity.ok(new UserPageResponse(
                userPage.users().stream().map(UserController::toResponse).toList(),
                userPage.total(),
                userPage.page(),
                userPage.size(),
                userPage.totalPages()
        ));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserCreateRequest request) {
        User created = userService.create(toUser(request), request.password());
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateById(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {
        User updated = userService.updateById(toUser(id, request));
        return updated == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        return userService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(UserConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(UserConflictException exception) {
        return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
    }

    private static User toUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        return user;
    }

    private static User toUser(Long id, UserUpdateRequest request) {
        User user = new User();
        user.setId(id);
        user.setNickname(request.nickname());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setStatus(request.status());
        return user;
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedTime(),
                user.getUpdatedTime()
        );
    }
}
