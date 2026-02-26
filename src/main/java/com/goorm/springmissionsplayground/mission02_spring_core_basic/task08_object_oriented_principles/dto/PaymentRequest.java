package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

public class PaymentRequest {

    private String orderId;
    private int amount;
    private String method;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
