package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto;

public class CategoryStockSummary {

    private String category;
    private long productCount;
    private long totalStockQuantity;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }

    public long getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public void setTotalStockQuantity(long totalStockQuantity) {
        this.totalStockQuantity = totalStockQuantity;
    }
}
