package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.payment;

public interface PaymentGateway {

    PaymentReceipt pay(int amount);
}
