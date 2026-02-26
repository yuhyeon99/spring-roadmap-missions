package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import org.springframework.stereotype.Component;

@Component("cardPaymentProcessor")
public class CardPaymentProcessor implements PaymentProcessor {

    @Override
    public String methodKey() {
        return "card";
    }

    @Override
    public PaymentExecution pay(int amount) {
        int fee = amount * 3 / 100;
        int approvedAmount = amount - fee;
        return new PaymentExecution(fee, approvedAmount, "카드 결제 수수료 3% 적용");
    }
}
