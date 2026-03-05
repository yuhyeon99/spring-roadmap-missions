package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment;

public record PaymentReceipt(String transactionId, int paidAmount) {

    public PaymentReceipt {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("거래 ID는 필수입니다.");
        }
        if (paidAmount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
    }
}
