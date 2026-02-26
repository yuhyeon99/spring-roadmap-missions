package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy;

import org.springframework.stereotype.Component;

@Component("fixedDiscountPolicy")
public class FixedDiscountPolicy implements DiscountPolicy {

    private static final int FIXED_DISCOUNT_AMOUNT = 1000;

    @Override
    public int discount(int amount) {
        return Math.min(amount, FIXED_DISCOUNT_AMOUNT);
    }

    @Override
    public String beanName() {
        return "fixedDiscountPolicy";
    }
}
