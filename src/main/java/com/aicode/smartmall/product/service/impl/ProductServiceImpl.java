package com.aicode.smartmall.product.service.impl;

import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.mapper.ProductMapper;
import com.aicode.smartmall.product.service.ProductService;
import com.aicode.smartmall.product.service.model.ProductPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Product getById(Long id) {
        validateId(id);
        return productMapper.selectById(id);
    }

    @Override
    public ProductPage getPage(int page, int size, Integer status, String keyword) {
        String normalizedKeyword = normalizeAndValidateQuery(page, size, status, keyword);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(status != null, Product::getStatus, status)
                .like(normalizedKeyword != null, Product::getName, normalizedKeyword)
                .orderByDesc(Product::getId);

        Page<Product> result = productMapper.selectPage(new Page<>(page, size), queryWrapper);
        return new ProductPage(
                result.getRecords(),
                result.getTotal(),
                page,
                size,
                result.getPages()
        );
    }

    @Override
    public Product create(Product product) {
        validateProductForCreate(product);

        product.setId(null);
        product.setDeleted(null);
        product.setCreatedTime(null);
        product.setUpdatedTime(null);

        productMapper.insert(product);
        return productMapper.selectById(product.getId());
    }

    @Override
    public Product updateById(Product product) {
        validateProductForUpdate(product);

        product.setDeleted(null);
        product.setCreatedTime(null);
        product.setUpdatedTime(null);

        if (productMapper.updateById(product) == 0) {
            return null;
        }
        return productMapper.selectById(product.getId());
    }

    @Override
    public boolean deleteById(Long id) {
        validateId(id);
        return productMapper.deleteById(id) == 1;
    }

    private static void validateProductForCreate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        validateName(product.getName());
        validatePrice(product.getPrice());
        validateStock(product.getStock());
        validateStatus(product.getStatus());
    }

    private static void validateProductForUpdate(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null");
        }
        validateId(product.getId());

        if (product.getName() != null) {
            validateName(product.getName());
        }
        if (product.getPrice() != null) {
            validatePrice(product.getPrice());
        }
        if (product.getStock() != null) {
            validateStock(product.getStock());
        }
        if (product.getStatus() != null) {
            validateStatus(product.getStatus());
        }

        if (product.getName() == null
                && product.getMainImageUrl() == null
                && product.getPrice() == null
                && product.getStock() == null
                && product.getDescription() == null
                && product.getStatus() == null) {
            throw new IllegalArgumentException("At least one product field must be provided for update");
        }
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Product id must be positive");
        }
    }

    private static String normalizeAndValidateQuery(
            int page,
            int size,
            Integer status,
            String keyword) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be at least 1");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (status != null && status != 0 && status != 1) {
            throw new IllegalArgumentException("Product status must be 0 or 1");
        }

        String normalizedKeyword = keyword == null ? null : keyword.trim();
        if (normalizedKeyword != null && normalizedKeyword.length() > 100) {
            throw new IllegalArgumentException("Product keyword must not exceed 100 characters");
        }
        return normalizedKeyword == null || normalizedKeyword.isEmpty() ? null : normalizedKeyword;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Product name must not exceed 200 characters");
        }
    }

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Product price must not be negative");
        }
        if (price.scale() > 2 || price.precision() - price.scale() > 8) {
            throw new IllegalArgumentException("Product price must fit DECIMAL(10,2)");
        }
    }

    private static void validateStock(Long stock) {
        if (stock == null || stock < 0 || stock > 4_294_967_295L) {
            throw new IllegalArgumentException("Product stock must fit INT UNSIGNED");
        }
    }

    private static void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("Product status must be 0 or 1");
        }
    }
}
