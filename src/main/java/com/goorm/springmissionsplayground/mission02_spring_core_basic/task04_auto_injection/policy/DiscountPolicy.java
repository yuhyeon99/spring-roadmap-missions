package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy;

public interface DiscountPolicy {

    int discount(int amount);

    String beanName();
}
