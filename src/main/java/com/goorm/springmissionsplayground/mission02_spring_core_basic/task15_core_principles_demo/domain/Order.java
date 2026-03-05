package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.domain;

public class Order {

    private final Long id;
    private final String item;
    private final int price;

    public Order(Long id, String item, int price) {
        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("상품명은 비어 있을 수 없습니다.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        this.id = id;
        this.item = item;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }
}
