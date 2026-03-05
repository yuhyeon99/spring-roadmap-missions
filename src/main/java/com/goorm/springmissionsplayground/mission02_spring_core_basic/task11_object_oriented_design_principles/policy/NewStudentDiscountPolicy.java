package com.goorm.springmissionsplayground.mission02_spring_core_basic.task11_object_oriented_design_principles.policy;

public class NewStudentDiscountPolicy implements DiscountPolicy {

    private final int discountRate;

    public NewStudentDiscountPolicy() {
        this(20);
    }

    public NewStudentDiscountPolicy(int discountRate) {
        if (discountRate < 0 || discountRate >= 100) {
            throw new IllegalArgumentException("할인율은 0 이상 100 미만이어야 합니다.");
        }
        this.discountRate = discountRate;
    }

    @Override
    public int apply(int baseAmount) {
        if (baseAmount <= 0) {
            throw new IllegalArgumentException("기준 금액은 0보다 커야 합니다.");
        }
        int discount = (baseAmount * discountRate) / 100;
        return baseAmount - discount;
    }

    public int getDiscountRate() {
        return discountRate;
    }
}
