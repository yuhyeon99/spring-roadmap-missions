package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto;

public class ProductResponse {

    private final Long id;
    private final String name;
    private final int price;
    private final String category;
    private final String message;

    public ProductResponse(Long id, String name, int price, String category, String message) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }
}
