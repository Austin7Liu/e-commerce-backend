package com.aicode.smartmall.category.exception;

public class CategoryInUseException extends RuntimeException {

    private final long childCount;
    private final long productCount;

    public CategoryInUseException(long childCount, long productCount) {
        super("Category cannot be deleted because it has " + childCount
                + " child categories and " + productCount + " associated products");
        this.childCount = childCount;
        this.productCount = productCount;
    }

    public long getChildCount() {
        return childCount;
    }

    public long getProductCount() {
        return productCount;
    }
}
