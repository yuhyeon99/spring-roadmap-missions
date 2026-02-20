package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto;

public class CircularDependencyResponse {

    private final String orderId;
    private final String orderState;
    private final String paymentState;
    private final String resolution;

    public CircularDependencyResponse(
            String orderId,
            String orderState,
            String paymentState,
            String resolution
    ) {
        this.orderId = orderId;
        this.orderState = orderState;
        this.paymentState = paymentState;
        this.resolution = resolution;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderState() {
        return orderState;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public String getResolution() {
        return resolution;
    }
}
