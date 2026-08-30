package com.aicode.smartmall.product.service;

import com.aicode.smartmall.product.entity.Product;
import com.aicode.smartmall.product.service.model.ProductPage;

public interface ProductService {

    Product getById(Long id);

    ProductPage getPage(int page, int size, Integer status, String keyword);

    Product create(Product product);

    Product updateById(Product product);

    boolean deleteById(Long id);
}
