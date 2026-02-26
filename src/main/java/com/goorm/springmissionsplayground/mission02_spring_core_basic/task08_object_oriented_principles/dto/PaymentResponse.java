package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

public class PaymentResponse {

    private final String orderId;
    private final String method;
    private final String processorBean;
    private final int requestedAmount;
    private final int fee;
    private final int approvedAmount;
    private final String message;

    public PaymentResponse(
            String orderId,
            String method,
            String processorBean,
            int requestedAmount,
            int fee,
            int approvedAmount,
            String message
    ) {
        this.orderId = orderId;
        this.method = method;
        this.processorBean = processorBean;
        this.requestedAmount = requestedAmount;
        this.fee = fee;
        this.approvedAmount = approvedAmount;
        this.message = message;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getMethod() {
        return method;
    }

    public String getProcessorBean() {
        return processorBean;
    }

    public int getRequestedAmount() {
        return requestedAmount;
    }

    public int getFee() {
        return fee;
    }

    public int getApprovedAmount() {
        return approvedAmount;
    }

    public String getMessage() {
        return message;
    }
}
