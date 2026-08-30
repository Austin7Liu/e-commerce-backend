package com.aicode.smartmall.user.service;

import com.aicode.smartmall.user.entity.User;
import com.aicode.smartmall.user.exception.UserConflictException;
import com.aicode.smartmall.user.service.model.UserPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateHashPasswordQueryUpdateAndDeleteUser() {
        User created = userService.create(user("service_user", "Service User"), "Password123");

        assertNotNull(created.getId());
        assertNotEquals("Password123", created.getPasswordHash());
        assertTrue(created.getPasswordHash().startsWith("pbkdf2_sha256$210000$"));
        assertEquals(1, created.getStatus());
        assertEquals(0, created.getDeleted());
        assertNotNull(created.getCreatedTime());

        User update = new User();
        update.setId(created.getId());
        update.setNickname("Updated Service User");
        update.setEmail("UPDATED@EXAMPLE.COM");
        update.setStatus(0);
        User updated = userService.updateById(update);

        assertNotNull(updated);
        assertEquals("Updated Service User", updated.getNickname());
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals(0, updated.getStatus());
        assertEquals(created.getPasswordHash(), updated.getPasswordHash());

        assertTrue(userService.deleteById(created.getId()));
        assertNull(userService.getById(created.getId()));
        assertFalse(userService.deleteById(created.getId()));
    }

    @Test
    void shouldRejectDuplicateUsernamePhoneAndEmail() {
        User first = user("unique_user", "Unique User");
        first.setPhone("13800000002");
        first.setEmail("unique@example.com");
        userService.create(first, "Password123");

        assertThrows(UserConflictException.class,
                () -> userService.create(user("unique_user", "Other User"), "Password123"));

        User samePhone = user("unique_phone_user", "Same Phone User");
        samePhone.setPhone("13800000002");
        assertThrows(UserConflictException.class,
                () -> userService.create(samePhone, "Password123"));

        User sameEmail = user("unique_email_user", "Same Email User");
        sameEmail.setEmail("UNIQUE@EXAMPLE.COM");
        assertThrows(UserConflictException.class,
                () -> userService.create(sameEmail, "Password123"));
    }

    @Test
    void shouldPageUsersByStatusAndKeyword() {
        userService.create(user("search_alice", "Alice Mall User"), "Password123");
        User disabled = user("search_disabled", "Alice Disabled User");
        disabled = userService.create(disabled, "Password123");
        User disableUpdate = new User();
        disableUpdate.setId(disabled.getId());
        disableUpdate.setStatus(0);
        userService.updateById(disableUpdate);

        UserPage page = userService.getPage(1, 10, 1, "  Alice Mall  ");

        assertEquals(1, page.users().size());
        assertEquals("search_alice", page.users().getFirst().getUsername());
        assertEquals(1, page.total());
        assertEquals(1, page.totalPages());
    }

    @Test
    void shouldRejectInvalidInputAndPageParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(user("ab", "Short Username"), "Password123"));
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(user("valid_user", "Valid User"), "short"));
        User invalidEmail = user("invalid_email_user", "Invalid Email User");
        invalidEmail.setEmail("invalid@@example.com");
        assertThrows(IllegalArgumentException.class,
                () -> userService.create(invalidEmail, "Password123"));
        assertThrows(IllegalArgumentException.class,
                () -> userService.getPage(0, 20, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> userService.getPage(1, 101, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> userService.getPage(1, 20, 2, null));
    }

    @Test
    void shouldExcludeLogicallyDeletedUserFromPage() {
        User user = userService.create(user("deleted_user", "Deleted Search User"), "Password123");
        assertTrue(userService.deleteById(user.getId()));

        UserPage page = userService.getPage(1, 20, null, "Deleted Search");

        assertTrue(page.users().isEmpty());
        assertEquals(0, page.total());
    }

    private User user(String username, String nickname) {
        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        return user;
    }
}
