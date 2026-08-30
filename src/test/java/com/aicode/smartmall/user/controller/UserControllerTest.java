package com.aicode.smartmall.user.controller;

import com.aicode.smartmall.user.entity.User;
import com.aicode.smartmall.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUserWithoutExposingPassword() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "controller_user",
                                  "password": "Password123",
                                  "nickname": "Controller User",
                                  "phone": "13800000003",
                                  "email": "CONTROLLER@EXAMPLE.COM"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("controller_user"))
                .andExpect(jsonPath("$.email").value("controller@example.com"))
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldQueryUpdateAndDeleteUser() throws Exception {
        User user = createUser("controller_operation", "Controller Operation");

        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("controller_operation"));

        mockMvc.perform(patch("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "Updated Controller User",
                                  "status": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Updated Controller User"))
                .andExpect(jsonPath("$.status").value(0));

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnPagedUsersFilteredByKeyword() throws Exception {
        User matching = createUser("controller_alice", "Alice Search User");
        createUser("controller_bob", "Bob Search User");

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10")
                        .param("status", "1")
                        .param("keyword", "Alice Search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(1))
                .andExpect(jsonPath("$.users[0].id").value(matching.getId()))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void shouldReturnConflictForDuplicateUsername() throws Exception {
        createUser("duplicate_controller", "First User");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "duplicate_controller",
                                  "password": "Password123",
                                  "nickname": "Duplicate User"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "password": "short",
                                  "nickname": "Invalid User"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private User createUser(String username, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        return userService.create(user, "Password123");
    }
}
