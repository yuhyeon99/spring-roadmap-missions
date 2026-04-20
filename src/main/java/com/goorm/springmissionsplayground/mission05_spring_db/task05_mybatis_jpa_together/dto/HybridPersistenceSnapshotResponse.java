package com.goorm.springmissionsplayground.mission05_spring_db.task05_mybatis_jpa_together.dto;

import java.util.List;

public class HybridPersistenceSnapshotResponse {

    private final String writeTechnology;
    private final String readTechnology;
    private final HybridStoreProductResponse savedByJpa;
    private final HybridStoreProductView readByMyBatis;
    private final List<CategoryStockSummary> categoryStockSummaries;

    public HybridPersistenceSnapshotResponse(
            String writeTechnology,
            String readTechnology,
            HybridStoreProductResponse savedByJpa,
            HybridStoreProductView readByMyBatis,
            List<CategoryStockSummary> categoryStockSummaries
    ) {
        this.writeTechnology = writeTechnology;
        this.readTechnology = readTechnology;
        this.savedByJpa = savedByJpa;
        this.readByMyBatis = readByMyBatis;
        this.categoryStockSummaries = categoryStockSummaries;
    }

    public String getWriteTechnology() {
        return writeTechnology;
    }

    public String getReadTechnology() {
        return readTechnology;
    }

    public HybridStoreProductResponse getSavedByJpa() {
        return savedByJpa;
    }

    public HybridStoreProductView getReadByMyBatis() {
        return readByMyBatis;
    }

    public List<CategoryStockSummary> getCategoryStockSummaries() {
        return categoryStockSummaries;
    }
}
