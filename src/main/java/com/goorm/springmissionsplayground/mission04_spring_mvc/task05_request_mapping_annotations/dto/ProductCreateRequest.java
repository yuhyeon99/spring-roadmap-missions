package com.goorm.springmissionsplayground.mission04_spring_mvc.task05_request_mapping_annotations.dto;

public class ProductCreateRequest {

    private String name;
    private int price;
    private String category;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
