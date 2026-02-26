package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

public class PaymentExecution {

    private final int fee;
    private final int approvedAmount;
    private final String detail;

    public PaymentExecution(int fee, int approvedAmount, String detail) {
        this.fee = fee;
        this.approvedAmount = approvedAmount;
        this.detail = detail;
    }

    public int getFee() {
        return fee;
    }

    public int getApprovedAmount() {
        return approvedAmount;
    }

    public String getDetail() {
        return detail;
    }
}
