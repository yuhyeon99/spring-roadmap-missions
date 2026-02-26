package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.policy.DiscountPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class QualifierInjectionService {

    private final DiscountPolicy discountPolicy;
    private final AmountFormatter amountFormatter;

    @Autowired
    public QualifierInjectionService(
            @Qualifier("fixedDiscountPolicy") DiscountPolicy discountPolicy,
            AmountFormatter amountFormatter
    ) {
        this.discountPolicy = discountPolicy;
        this.amountFormatter = amountFormatter;
    }

    public InjectionCaseResult compare(int amount) {
        int discounted = discountPolicy.discount(amount);
        return new InjectionCaseResult(
                "@Autowired + @Qualifier",
                discountPolicy.beanName(),
                "할인 금액: " + amountFormatter.format(discounted),
                "동일 타입 빈이 여러 개일 때 이름으로 주입 대상을 지정"
        );
    }
}
