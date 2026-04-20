package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto;

import java.util.List;

public class HybridTechnologyComparisonResponse {

    private final String jpaTechnology;
    private final String myBatisTechnology;
    private final HybridStoreProductResponse jpaProduct;
    private final HybridStoreProductView myBatisProduct;
    private final List<CategoryStockSummary> categoryStockSummaries;

    public HybridTechnologyComparisonResponse(
            String jpaTechnology,
            String myBatisTechnology,
            HybridStoreProductResponse jpaProduct,
            HybridStoreProductView myBatisProduct,
            List<CategoryStockSummary> categoryStockSummaries
    ) {
        this.jpaTechnology = jpaTechnology;
        this.myBatisTechnology = myBatisTechnology;
        this.jpaProduct = jpaProduct;
        this.myBatisProduct = myBatisProduct;
        this.categoryStockSummaries = categoryStockSummaries;
    }

    public String getJpaTechnology() {
        return jpaTechnology;
    }

    public String getMyBatisTechnology() {
        return myBatisTechnology;
    }

    public HybridStoreProductResponse getJpaProduct() {
        return jpaProduct;
    }

    public HybridStoreProductView getMyBatisProduct() {
        return myBatisProduct;
    }

    public List<CategoryStockSummary> getCategoryStockSummaries() {
        return categoryStockSummaries;
    }
}
