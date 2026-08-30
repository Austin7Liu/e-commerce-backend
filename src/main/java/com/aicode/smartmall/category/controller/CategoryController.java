package com.aicode.smartmall.category.controller;

import com.aicode.smartmall.category.dto.CategoryCreateRequest;
import com.aicode.smartmall.category.dto.CategoryListQueryRequest;
import com.aicode.smartmall.category.dto.CategoryPageResponse;
import com.aicode.smartmall.category.dto.CategoryResponse;
import com.aicode.smartmall.category.dto.CategoryUpdateRequest;
import com.aicode.smartmall.category.entity.Category;
import com.aicode.smartmall.category.exception.CategoryInUseException;
import com.aicode.smartmall.category.service.CategoryService;
import com.aicode.smartmall.category.service.model.CategoryPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return category == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(toResponse(category));
    }

    @GetMapping
    public List<CategoryResponse> getChildren(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer status) {
        return categoryService.getChildren(parentId, status).stream()
                .map(CategoryController::toResponse)
                .toList();
    }

    @GetMapping("/page")
    public ResponseEntity<CategoryPageResponse> getPage(@ModelAttribute CategoryListQueryRequest request) {
        int page = request.page() == null ? 1 : request.page();
        int size = request.size() == null ? 20 : request.size();
        CategoryPage categoryPage = categoryService.getPage(
                page,
                size,
                request.parentId(),
                request.status(),
                request.name()
        );
        CategoryPageResponse response = new CategoryPageResponse(
                categoryPage.categories().stream().map(CategoryController::toResponse).toList(),
                categoryPage.total(),
                categoryPage.page(),
                categoryPage.size(),
                categoryPage.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryCreateRequest request) {
        Category created = categoryService.create(toCategory(request));
        return ResponseEntity.created(URI.create("/api/categories/" + created.getId()))
                .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateById(
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequest request) {
        Category updated = categoryService.updateById(toCategory(id, request));
        return updated == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        return categoryService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryInUse(CategoryInUseException exception) {
        return ResponseEntity.status(409).body(Map.of(
                "message", exception.getMessage(),
                "childCount", exception.getChildCount(),
                "productCount", exception.getProductCount()
        ));
    }

    private static Category toCategory(CategoryCreateRequest request) {
        Category category = new Category();
        category.setParentId(request.parentId());
        category.setName(request.name());
        category.setSortOrder(request.sortOrder());
        category.setStatus(request.status());
        return category;
    }

    private static Category toCategory(Long id, CategoryUpdateRequest request) {
        Category category = new Category();
        category.setId(id);
        category.setParentId(request.parentId());
        category.setName(request.name());
        category.setSortOrder(request.sortOrder());
        category.setStatus(request.status());
        return category;
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getParentId(),
                category.getName(),
                category.getSortOrder(),
                category.getStatus(),
                category.getCreatedTime(),
                category.getUpdatedTime()
        );
    }
}
