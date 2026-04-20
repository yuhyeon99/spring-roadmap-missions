package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class HybridStoreProductCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 40, message = "상품명은 40자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 30, message = "카테고리는 30자 이하여야 합니다.")
    private String category;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private int price;

    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private int stockQuantity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
