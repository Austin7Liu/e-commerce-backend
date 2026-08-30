package com.aicode.smartmall.product.controller;

import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;

    @Test
    void shouldCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Controller create test product",
                                  "mainImageUrl": null,
                                  "price": 89.90,
                                  "stock": 12,
                                  "description": "Created through the product API",
                                  "status": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Controller create test product"))
                .andExpect(jsonPath("$.mainImageUrl").doesNotExist())
                .andExpect(jsonPath("$.price").value(89.90))
                .andExpect(jsonPath("$.stock").value(12))
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.createdTime").exists())
                .andExpect(jsonPath("$.updatedTime").exists());
    }

    @Test
    void shouldQueryUpdateAndDeleteProduct() throws Exception {
        Product product = createProduct("Controller operation test product");
        Long productId = product.getId();

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Controller operation test product"));

        mockMvc.perform(patch("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated controller test product",
                                  "price": 119.90,
                                  "status": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated controller test product"))
                .andExpect(jsonPath("$.price").value(119.90))
                .andExpect(jsonPath("$.status").value(1));

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product id must be positive"));
    }

    @Test
    void shouldReturnPagedProductList() throws Exception {
        Product firstProduct = createProduct("First controller page product");
        Product secondProduct = createProduct("Second controller page product");

        mockMvc.perform(get("/api/products")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].id").value(secondProduct.getId()))
                .andExpect(jsonPath("$.products[1].id").value(firstProduct.getId()));
    }

    @Test
    void shouldReturnBadRequestForInvalidPageParameters() throws Exception {
        mockMvc.perform(get("/api/products").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));
    }

    @Test
    void shouldFilterProductListByStatusAndKeyword() throws Exception {
        Product matchingProduct = createProduct("Unique controller Bluetooth product");
        Product otherStatusProduct = new Product();
        otherStatusProduct.setName("Unique controller Bluetooth offline product");
        otherStatusProduct.setPrice(new BigDecimal("129.90"));
        otherStatusProduct.setStock(8L);
        otherStatusProduct.setStatus(1);
        productService.create(otherStatusProduct);

        mockMvc.perform(get("/api/products")
                        .param("status", "0")
                        .param("keyword", "controller Bluetooth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.length()").value(1))
                .andExpect(jsonPath("$.products[0].id").value(matchingProduct.getId()));
    }

    private Product createProduct(String name) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(new BigDecimal("99.90"));
        product.setStock(10L);
        product.setStatus(0);
        return productService.create(product);
    }
}
