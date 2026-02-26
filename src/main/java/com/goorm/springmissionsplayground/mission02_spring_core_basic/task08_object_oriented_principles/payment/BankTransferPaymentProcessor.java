package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import org.springframework.stereotype.Component;

@Component("bankTransferPaymentProcessor")
public class BankTransferPaymentProcessor implements PaymentProcessor {

    @Override
    public String methodKey() {
        return "bank";
    }

    @Override
    public PaymentExecution pay(int amount) {
        int fee = Math.min(amount, 500);
        int approvedAmount = amount - fee;
        return new PaymentExecution(fee, approvedAmount, "계좌이체 고정 수수료 500원 적용");
    }
}
