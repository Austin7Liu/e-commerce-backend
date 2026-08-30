package com.aicode.smartmall.category.service;

import com.aicode.smartmall.category.entity.Category;

import java.util.List;

public interface CategoryService {

    Category getById(Long id);

    List<Category> getChildren(Long parentId, Integer status);

    Category create(Category category);

    Category updateById(Category category);

    boolean deleteById(Long id);
}
