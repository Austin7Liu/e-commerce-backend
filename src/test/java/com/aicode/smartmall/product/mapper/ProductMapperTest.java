package com.aicode.smartmall.product.mapper;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.mapper.CategoryMapper;
import com.aicode.smartmall.product.entity.Product;
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
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void shouldMapProductFieldsAndSupportBaseMapperOperations() {
        Product product = new Product();
        Category category = new Category();
        category.setName("Product mapper test category");
        category.setSortOrder(0);
        category.setStatus(1);
        assertEquals(1, categoryMapper.insert(category));
        product.setCategoryId(category.getId());
        product.setName("Mapper mapping test product");
        product.setMainImageUrl(null);
        product.setPrice(new BigDecimal("99.90"));
        product.setStock(10L);
        product.setDescription("Product used to verify MyBatis-Plus mapping");
        product.setStatus(0);

        assertEquals(1, productMapper.insert(product));
        assertNotNull(product.getId());

        Product savedProduct = productMapper.selectById(product.getId());

        assertNotNull(savedProduct);
        assertEquals(product.getName(), savedProduct.getName());
        assertNull(savedProduct.getMainImageUrl());
        assertEquals(0, product.getPrice().compareTo(savedProduct.getPrice()));
        assertEquals(product.getStock(), savedProduct.getStock());
        assertEquals(product.getDescription(), savedProduct.getDescription());
        assertEquals(product.getStatus(), savedProduct.getStatus());
        assertEquals(0, savedProduct.getDeleted());
        assertNotNull(savedProduct.getCreatedTime());
        assertNotNull(savedProduct.getUpdatedTime());

        assertEquals(1, productMapper.deleteById(product.getId()));
        assertNull(productMapper.selectById(product.getId()));
    }
}
