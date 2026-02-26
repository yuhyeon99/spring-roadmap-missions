package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.payment;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto.PaymentExecution;

public interface PaymentProcessor {

    String methodKey();

    PaymentExecution pay(int amount);
}
