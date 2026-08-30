package com.aicode.smartmall.product.service;

import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.service.CategoryService;
import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.service.model.ProductPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Test
    void shouldCreateQueryUpdateAndDeleteProduct() {
        Product product = new Product();
        product.setCategoryId(createCategory("Service operation category").getId());
        product.setName("Service test product");
        product.setPrice(new BigDecimal("199.90"));
        product.setStock(20L);
        product.setStatus(0);

        Product createdProduct = productService.create(product);

        assertNotNull(createdProduct);
        assertNotNull(createdProduct.getId());
        assertEquals(0, createdProduct.getDeleted());
        assertNotNull(createdProduct.getCreatedTime());
        assertNotNull(createdProduct.getUpdatedTime());

        Product queriedProduct = productService.getById(createdProduct.getId());
        assertNotNull(queriedProduct);
        assertEquals("Service test product", queriedProduct.getName());

        Product update = new Product();
        update.setId(createdProduct.getId());
        update.setName("Updated service test product");
        update.setPrice(new BigDecimal("209.90"));
        update.setStatus(1);
        update.setCategoryId(createCategory("Service active product category").getId());

        Product updatedProduct = productService.updateById(update);

        assertNotNull(updatedProduct);
        assertEquals("Updated service test product", updatedProduct.getName());
        assertEquals(0, new BigDecimal("209.90").compareTo(updatedProduct.getPrice()));
        assertEquals(1, updatedProduct.getStatus());
        assertEquals(createdProduct.getStock(), updatedProduct.getStock());

        assertTrue(productService.deleteById(createdProduct.getId()));
        assertNull(productService.getById(createdProduct.getId()));
        assertFalse(productService.deleteById(createdProduct.getId()));
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> productService.getById(0L));

        Product invalidProduct = new Product();
        invalidProduct.setCategoryId(createCategory("Service invalid input category").getId());
        invalidProduct.setName(" ");
        invalidProduct.setPrice(new BigDecimal("10.00"));
        invalidProduct.setStock(1L);
        invalidProduct.setStatus(0);

        assertThrows(IllegalArgumentException.class, () -> productService.create(invalidProduct));
    }

    @Test
    void shouldReturnProductPageOrderedByIdDescending() {
        Product firstProduct = createProduct("First page service product");
        Product secondProduct = createProduct("Second page service product");

        ProductPage productPage = productService.getPage(1, 2, null, null);

        assertEquals(1, productPage.page());
        assertEquals(2, productPage.size());
        assertEquals(2, productPage.products().size());
        assertTrue(productPage.total() >= 2);
        assertEquals(secondProduct.getId(), productPage.products().get(0).getId());
        assertEquals(firstProduct.getId(), productPage.products().get(1).getId());
    }

    @Test
    void shouldRejectInvalidPageParameters() {
        assertThrows(IllegalArgumentException.class, () -> productService.getPage(0, 20, null, null));
        assertThrows(IllegalArgumentException.class, () -> productService.getPage(1, 101, null, null));
        assertThrows(IllegalArgumentException.class, () -> productService.getPage(1, 20, 2, null));
        assertThrows(IllegalArgumentException.class,
                () -> productService.getPage(1, 20, null, "a".repeat(101)));
    }

    @Test
    void shouldFilterProductPageByStatusAndName() {
        Product matchingProduct = createProduct("Unique filtered Bluetooth product");
        Product otherStatusProduct = new Product();
        otherStatusProduct.setName("Unique filtered Bluetooth offline product");
        otherStatusProduct.setPrice(new BigDecimal("59.90"));
        otherStatusProduct.setStock(3L);
        otherStatusProduct.setStatus(1);
        otherStatusProduct.setCategoryId(createCategory("Service filter category").getId());
        productService.create(otherStatusProduct);

        ProductPage productPage = productService.getPage(1, 20, 0, "  filtered Bluetooth  ");

        assertEquals(1, productPage.products().size());
        assertEquals(matchingProduct.getId(), productPage.products().get(0).getId());
    }

    @Test
    void shouldExcludeLogicallyDeletedProductsFromPage() {
        Product product = createProduct("Unique logically deleted list product");
        assertTrue(productService.deleteById(product.getId()));

        ProductPage productPage = productService.getPage(1, 20, null, "logically deleted list");

        assertTrue(productPage.products().isEmpty());
        assertEquals(0, productPage.total());
    }

    private Product createProduct(String name) {
        Product product = new Product();
        product.setCategoryId(createCategory(name + " category").getId());
        product.setName(name);
        product.setPrice(new BigDecimal("49.90"));
        product.setStock(5L);
        product.setStatus(0);
        return productService.create(product);
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setSortOrder(0);
        category.setStatus(1);
        return categoryService.create(category);
    }
}
