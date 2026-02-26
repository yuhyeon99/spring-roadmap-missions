package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component("rateDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy {

    private static final int DISCOUNT_RATE = 10;

    @Override
    public int discount(int amount) {
        return amount * DISCOUNT_RATE / 100;
    }

    @Override
    public String beanName() {
        return "rateDiscountPolicy";
    }
}
