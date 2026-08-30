package com.aicode.smartmall.category.service;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.exception.CategoryInUseException;
import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateQueryUpdateAndLogicallyDeleteEmptyCategory() {
        Category category = createCategory(null, "Category service operation", 2, 1);

        assertNotNull(categoryService.getById(category.getId()));

        Category update = new Category();
        update.setId(category.getId());
        update.setName("Updated category service operation");
        update.setSortOrder(3);
        Category updated = categoryService.updateById(update);

        assertNotNull(updated);
        assertEquals("Updated category service operation", updated.getName());
        assertEquals(3, updated.getSortOrder());
        assertTrue(categoryService.deleteById(category.getId()));
        assertNull(categoryService.getById(category.getId()));
        assertFalse(categoryService.deleteById(category.getId()));
    }

    @Test
    void shouldListRootAndDirectChildrenInConfiguredOrder() {
        Category root = createCategory(null, "Category list root", 0, 1);
        Category second = createCategory(root.getId(), "Category list second", 20, 1);
        Category first = createCategory(root.getId(), "Category list first", 10, 1);
        createCategory(root.getId(), "Category list disabled", 5, 0);

        List<Category> enabledChildren = categoryService.getChildren(root.getId(), 1);

        assertEquals(List.of(first.getId(), second.getId()),
                enabledChildren.stream().map(Category::getId).toList());
        assertTrue(categoryService.getChildren(null, null).stream()
                .anyMatch(category -> category.getId().equals(root.getId())));
    }

    @Test
    void shouldRejectParentCycle() {
        Category root = createCategory(null, "Category cycle root", 0, 1);
        Category child = createCategory(root.getId(), "Category cycle child", 0, 1);

        Category update = new Category();
        update.setId(root.getId());
        update.setParentId(child.getId());

        assertThrows(IllegalArgumentException.class, () -> categoryService.updateById(update));
    }

    @Test
    void shouldRejectDeletingCategoryWithChildOrProduct() {
        Category parent = createCategory(null, "Category delete parent", 0, 1);
        Category child = createCategory(parent.getId(), "Category delete child", 0, 1);

        CategoryInUseException childConflict = assertThrows(
                CategoryInUseException.class,
                () -> categoryService.deleteById(parent.getId()));
        assertEquals(1, childConflict.getChildCount());

        assertTrue(categoryService.deleteById(child.getId()));
        assertTrue(categoryService.deleteById(parent.getId()));

        Category productCategory = createCategory(null, "Category delete product", 0, 1);
        Product product = new Product();
        product.setCategoryId(productCategory.getId());
        product.setName("Category deletion blocking product");
        product.setPrice(new BigDecimal("19.90"));
        product.setStock(1L);
        product.setStatus(1);
        Product createdProduct = productService.create(product);

        CategoryInUseException productConflict = assertThrows(
                CategoryInUseException.class,
                () -> categoryService.deleteById(productCategory.getId()));
        assertEquals(1, productConflict.getProductCount());

        assertTrue(productService.deleteById(createdProduct.getId()));
        assertTrue(categoryService.deleteById(productCategory.getId()));
    }

    @Test
    void shouldValidateProductCategoryWhenPublishing() {
        Product noCategory = product("Product without category", null, 1);
        assertThrows(IllegalArgumentException.class, () -> productService.create(noCategory));

        Category disabled = createCategory(null, "Disabled product category", 0, 0);
        Product disabledCategoryProduct = product("Disabled category product", disabled.getId(), 1);
        assertThrows(IllegalArgumentException.class, () -> productService.create(disabledCategoryProduct));

        Product draft = product("Unclassified draft product", null, 0);
        assertNotNull(productService.create(draft));
    }

    private Category createCategory(Long parentId, String name, int sortOrder, int status) {
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setStatus(status);
        return categoryService.create(category);
    }

    private Product product(String name, Long categoryId, int status) {
        Product product = new Product();
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setPrice(new BigDecimal("9.90"));
        product.setStock(1L);
        product.setStatus(status);
        return product;
    }
}
