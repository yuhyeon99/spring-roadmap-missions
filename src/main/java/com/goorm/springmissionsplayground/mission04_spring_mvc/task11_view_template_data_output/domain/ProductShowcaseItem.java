package com.goorm.springmissionsplayground.mission04_spring_mvc.task11_view_template_data_output.domain;

public class ProductShowcaseItem {

    private final Long id;
    private final String name;
    private final String categoryCode;
    private final String categoryLabel;
    private final int price;
    private final int stockQuantity;
    private final boolean featured;
    private final String summary;

    public ProductShowcaseItem(
            Long id,
            String name,
            String categoryCode,
            String categoryLabel,
            int price,
            int stockQuantity,
            boolean featured,
            String summary
    ) {
        this.id = id;
        this.name = name;
        this.categoryCode = categoryCode;
        this.categoryLabel = categoryLabel;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.featured = featured;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public int getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isFeatured() {
        return featured;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isLowStock() {
        return stockQuantity <= 5;
    }

    public String getFormattedPrice() {
        return String.format("%,d원", price);
    }

    public String getStockStatusText() {
        return isLowStock() ? "재고 확인 필요" : "즉시 구매 가능";
    }
}
