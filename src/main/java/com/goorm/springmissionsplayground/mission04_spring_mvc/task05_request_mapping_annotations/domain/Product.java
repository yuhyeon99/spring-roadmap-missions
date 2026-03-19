package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.domain;

public class Product {

    private final Long id;
    private final String name;
    private final int price;
    private final String category;

    public Product(Long id, String name, int price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
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
}
