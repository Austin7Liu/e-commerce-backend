package com.aicode.smartmall.category.service.impl;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.exception.CategoryInUseException;
import com.aicode.smartmall.category.mapper.CategoryMapper;
import com.aicode.smartmall.category.service.CategoryService;
import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    @Override
    public Category getById(Long id) {
        validateId(id);
        return categoryMapper.selectById(id);
    }

    @Override
    public List<Category> getChildren(Long parentId, Integer status) {
        if (parentId != null) {
            validateId(parentId);
        }
        validateOptionalStatus(status);

        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<Category>()
                .isNull(parentId == null, Category::getParentId)
                .eq(parentId != null, Category::getParentId, parentId)
                .eq(status != null, Category::getStatus, status)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId);
        return categoryMapper.selectList(query);
    }

    @Override
    @Transactional
    public Category create(Category category) {
        validateForCreate(category);
        validateParent(category.getParentId(), null);

        category.setId(null);
        category.setDeleted(null);
        category.setCreatedTime(null);
        category.setUpdatedTime(null);
        categoryMapper.insert(category);
        return categoryMapper.selectById(category.getId());
    }

    @Override
    @Transactional
    public Category updateById(Category category) {
        validateForUpdate(category);
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            return null;
        }
        //子类目
        if (category.getParentId() != null) {
            validateParent(category.getParentId(), category.getId());
        }

        category.setDeleted(null);
        category.setCreatedTime(null);
        category.setUpdatedTime(null);
        if (categoryMapper.updateById(category) == 0) {
            return null;
        }
        return categoryMapper.selectById(category.getId());
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        validateId(id);
        if (categoryMapper.selectById(id) == null) {
            return false;
        }

        long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        //当前类目存在子类目或当前类目存在绑定的商品，抛异常
        if (childCount > 0 || productCount > 0) {
            throw new CategoryInUseException(childCount, productCount);
        }
        return categoryMapper.deleteById(id) == 1;
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        validateId(parentId);
        if (parentId.equals(currentId)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }

        Set<Long> visited = new HashSet<>();
        Long ancestorId = parentId;
        while (ancestorId != null) {
            if (!visited.add(ancestorId) || ancestorId.equals(currentId)) {
                throw new IllegalArgumentException("Category parent relationship cannot contain a cycle");
            }
            Category ancestor = categoryMapper.selectById(ancestorId);
            if (ancestor == null) {
                throw new IllegalArgumentException("Parent category does not exist");
            }
            ancestorId = ancestor.getParentId();
        }
    }

    private static void validateForCreate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        validateName(category.getName());
        validateSortOrder(category.getSortOrder());
        validateStatus(category.getStatus());
    }

    private static void validateForUpdate(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        validateId(category.getId());
        if (category.getName() != null) {
            validateName(category.getName());
        }
        if (category.getSortOrder() != null) {
            validateSortOrder(category.getSortOrder());
        }
        validateOptionalStatus(category.getStatus());
        if (category.getParentId() == null && category.getName() == null
                && category.getSortOrder() == null && category.getStatus() == null) {
            throw new IllegalArgumentException("At least one category field must be provided for update");
        }
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Category id must be positive");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Category name must not exceed 100 characters");
        }
    }

    private static void validateSortOrder(Integer sortOrder) {
        if (sortOrder == null || sortOrder < 0) {
            throw new IllegalArgumentException("Category sort order must not be negative");
        }
    }

    private static void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("Category status must be 0 or 1");
        }
    }

    private static void validateOptionalStatus(Integer status) {
        if (status != null) {
            validateStatus(status);
        }
    }
}
