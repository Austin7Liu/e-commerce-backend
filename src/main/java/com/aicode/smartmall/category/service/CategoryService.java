package com.aicode.smartmall.category.service;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.service.model.CategoryPage;

import java.util.List;

public interface CategoryService {

    Category getById(Long id);

    List<Category> getChildren(Long parentId, Integer status);

    CategoryPage getPage(int page, int size, Long parentId, Integer status, String name);

    Category create(Category category);

    Category updateById(Category category);

    boolean deleteById(Long id);
}
