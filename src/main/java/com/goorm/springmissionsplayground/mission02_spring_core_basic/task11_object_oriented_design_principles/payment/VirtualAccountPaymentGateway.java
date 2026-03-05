package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment;

import java.util.UUID;

public class VirtualAccountPaymentGateway implements PaymentGateway {

    @Override
    public PaymentReceipt pay(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
        String transactionId = "VA-" + UUID.randomUUID();
        return new PaymentReceipt(transactionId, amount);
    }
}
