package com.aicode.smartmall.user.service;

import com.aicode.smartmall.user.entity.User;
import com.aicode.smartmall.user.service.model.UserPage;

public interface UserService {

    User getById(Long id);

    UserPage getPage(int page, int size, Integer status, String keyword);

    User create(User user, String rawPassword);

    User updateById(User user);

    boolean deleteById(Long id);
}
