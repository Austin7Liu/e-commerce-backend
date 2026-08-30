package com.aicode.smartmall.category.mapper;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Test
    void shouldMapCategoryHierarchyAndProductRelationship() {
        Category rootCategory = createCategory(null, "Mapper test root category", 1);
        Category childCategory = createCategory(rootCategory.getId(), "Mapper test child category", 2);

        Category savedChildCategory = categoryMapper.selectById(childCategory.getId());

        assertNotNull(savedChildCategory);
        assertEquals(rootCategory.getId(), savedChildCategory.getParentId());
        assertEquals(2, savedChildCategory.getSortOrder());
        assertEquals(1, savedChildCategory.getStatus());
        assertEquals(0, savedChildCategory.getDeleted());
        assertNotNull(savedChildCategory.getCreatedTime());
        assertNotNull(savedChildCategory.getUpdatedTime());

        Product product = new Product();
        product.setCategoryId(childCategory.getId());
        product.setName("Mapper category relationship product");
        product.setPrice(new BigDecimal("79.90"));
        product.setStock(6L);
        product.setStatus(0);

        assertEquals(1, productMapper.insert(product));

        Product savedProduct = productMapper.selectById(product.getId());
        assertNotNull(savedProduct);
        assertEquals(childCategory.getId(), savedProduct.getCategoryId());

        assertEquals(1, categoryMapper.deleteById(childCategory.getId()));
        assertNull(categoryMapper.selectById(childCategory.getId()));
    }

    private Category createCategory(Long parentId, String name, int sortOrder) {
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setStatus(1);

        assertEquals(1, categoryMapper.insert(category));
        assertNotNull(category.getId());
        return category;
    }
}
