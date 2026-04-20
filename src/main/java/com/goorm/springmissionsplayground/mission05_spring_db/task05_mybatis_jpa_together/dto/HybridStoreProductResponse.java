package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto;

import com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.domain.HybridStoreProduct;

public class HybridStoreProductResponse {

    private final Long id;
    private final String name;
    private final String category;
    private final int price;
    private final int stockQuantity;

    public HybridStoreProductResponse(Long id, String name, String category, int price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public static HybridStoreProductResponse from(HybridStoreProduct product) {
        return new HybridStoreProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStockQuantity()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }
}
