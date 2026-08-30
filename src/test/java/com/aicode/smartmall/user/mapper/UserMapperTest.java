package com.aicode.smartmall.user.mapper;

import com.aicode.smartmall.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldMapUserFieldsAndSupportLogicalDelete() {
        User user = new User();
        user.setUsername("mapper_user");
        user.setPasswordHash("pbkdf2_sha256$test");
        user.setNickname("Mapper User");
        user.setPhone("13800000001");
        user.setEmail("mapper@example.com");
        user.setStatus(1);

        assertEquals(1, userMapper.insert(user));
        assertNotNull(user.getId());

        User saved = userMapper.selectById(user.getId());
        assertNotNull(saved);
        assertEquals("mapper_user", saved.getUsername());
        assertEquals("Mapper User", saved.getNickname());
        assertEquals("13800000001", saved.getPhone());
        assertEquals("mapper@example.com", saved.getEmail());
        assertEquals(0, saved.getDeleted());
        assertNotNull(saved.getCreatedTime());
        assertNotNull(saved.getUpdatedTime());

        assertEquals(1, userMapper.deleteById(user.getId()));
        assertNull(userMapper.selectById(user.getId()));
    }
}
