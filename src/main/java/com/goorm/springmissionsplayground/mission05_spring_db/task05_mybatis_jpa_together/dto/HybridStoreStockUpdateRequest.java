package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto;

import jakarta.validation.constraints.Min;

public class HybridStoreStockUpdateRequest {

    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private int stockQuantity;

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
