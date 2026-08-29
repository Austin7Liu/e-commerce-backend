package com.aicode.smartmall.product.controller;

import com.aicode.smartmall.product.dto.ProductCreateRequest;
import com.aicode.smartmall.product.dto.ProductPageResponse;
import com.aicode.smartmall.product.dto.ProductResponse;
import com.aicode.smartmall.product.dto.ProductUpdateRequest;
import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.service.ProductService;
import com.aicode.smartmall.product.service.model.ProductPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(product));
    }

    @GetMapping
    public ResponseEntity<ProductPageResponse> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductPage productPage = productService.getPage(page, size);
        ProductPageResponse response = new ProductPageResponse(
                productPage.products().stream().map(ProductController::toResponse).toList(),
                productPage.total(),
                productPage.page(),
                productPage.size(),
                productPage.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request) {
        Product createdProduct = productService.create(toProduct(request));
        return ResponseEntity
                .created(URI.create("/api/products/" + createdProduct.getId()))
                .body(toResponse(createdProduct));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateById(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request) {
        Product updatedProduct = productService.updateById(toProduct(id, request));
        if (updatedProduct == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!productService.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    private static Product toProduct(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setMainImageUrl(request.mainImageUrl());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setDescription(request.description());
        product.setStatus(request.status());
        return product;
    }

    private static Product toProduct(Long id, ProductUpdateRequest request) {
        Product product = new Product();
        product.setId(id);
        product.setName(request.name());
        product.setMainImageUrl(request.mainImageUrl());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setDescription(request.description());
        product.setStatus(request.status());
        return product;
    }

    private static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getMainImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getStatus(),
                product.getCreatedTime(),
                product.getUpdatedTime()
        );
    }
}
