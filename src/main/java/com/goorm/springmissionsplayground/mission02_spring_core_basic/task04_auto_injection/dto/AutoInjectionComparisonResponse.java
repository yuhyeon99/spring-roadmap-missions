package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto;

import java.util.List;

public class AutoInjectionComparisonResponse {

    private final int amount;
    private final List<InjectionCaseResult> comparisons;

    public AutoInjectionComparisonResponse(int amount, List<InjectionCaseResult> comparisons) {
        this.amount = amount;
        this.comparisons = comparisons;
    }

    public int getAmount() {
        return amount;
    }

    public List<InjectionCaseResult> getComparisons() {
        return comparisons;
    }
}
