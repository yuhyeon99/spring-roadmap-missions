package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;
import org.springframework.stereotype.Component;

@Component("kakaoPayPaymentProcessor")
public class KakaoPayPaymentProcessor implements PaymentProcessor {

    @Override
    public String methodKey() {
        return "kakaopay";
    }

    @Override
    public PaymentExecution pay(int amount) {
        int fee = amount * 2 / 100;
        int approvedAmount = amount - fee;
        return new PaymentExecution(fee, approvedAmount, "카카오페이 결제 수수료 2% 적용");
    }
}
