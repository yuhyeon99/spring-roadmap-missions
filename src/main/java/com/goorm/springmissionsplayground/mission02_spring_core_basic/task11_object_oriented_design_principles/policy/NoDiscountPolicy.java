package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.policy;

public class NoDiscountPolicy implements DiscountPolicy {

    @Override
    public int apply(int baseAmount) {
        if (baseAmount <= 0) {
            throw new IllegalArgumentException("기준 금액은 0보다 커야 합니다.");
        }
        return baseAmount;
    }
}
