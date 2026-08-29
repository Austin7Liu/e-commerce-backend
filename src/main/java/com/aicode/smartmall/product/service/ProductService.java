package com.aicode.smartmall.product.service;

import com.aicode.smartmall.product.entity.Product;

public interface ProductService {

    Product getById(Long id);

    Product create(Product product);

    Product updateById(Product product);

    boolean deleteById(Long id);
}
