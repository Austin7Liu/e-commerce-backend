package com.aicode.smartmall.product.service;

import com.aicode.smartmall.product.entity.Product;
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

    @Test
    void shouldCreateQueryUpdateAndDeleteProduct() {
        Product product = new Product();
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
        invalidProduct.setName(" ");
        invalidProduct.setPrice(new BigDecimal("10.00"));
        invalidProduct.setStock(1L);
        invalidProduct.setStatus(0);

        assertThrows(IllegalArgumentException.class, () -> productService.create(invalidProduct));
    }
}
