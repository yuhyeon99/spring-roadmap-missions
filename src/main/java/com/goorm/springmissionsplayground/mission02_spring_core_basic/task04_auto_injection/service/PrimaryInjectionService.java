package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy.DiscountPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrimaryInjectionService {

    private final DiscountPolicy discountPolicy;
    private final AmountFormatter amountFormatter;

    @Autowired
    public PrimaryInjectionService(DiscountPolicy discountPolicy, AmountFormatter amountFormatter) {
        this.discountPolicy = discountPolicy;
        this.amountFormatter = amountFormatter;
    }

    public InjectionCaseResult compare(int amount) {
        int discounted = discountPolicy.discount(amount);
        return new InjectionCaseResult(
                "@Autowired + @Primary",
                discountPolicy.beanName(),
                "할인 금액: " + amountFormatter.format(discounted),
                "@Primary가 지정된 빈을 기본 후보로 우선 선택"
        );
    }
}
