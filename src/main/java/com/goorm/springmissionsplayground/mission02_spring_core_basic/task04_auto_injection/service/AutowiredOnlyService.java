package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.InjectionCaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutowiredOnlyService {

    private final AmountFormatter amountFormatter;

    @Autowired
    public AutowiredOnlyService(AmountFormatter amountFormatter) {
        this.amountFormatter = amountFormatter;
    }

    public InjectionCaseResult compare(int amount) {
        return new InjectionCaseResult(
                "@Autowired",
                "amountFormatter",
                "포맷 결과: " + amountFormatter.format(amount),
                "동일 타입 빈이 1개라서 타입 기반 자동 주입"
        );
    }
}
