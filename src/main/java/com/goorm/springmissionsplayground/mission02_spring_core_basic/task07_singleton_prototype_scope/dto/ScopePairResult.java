package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto;

public class ScopePairResult {

    private final String firstInstanceId;
    private final int firstCallCount;
    private final String secondInstanceId;
    private final int secondCallCount;
    private final boolean sameInstance;
    private final String explanation;

    public ScopePairResult(
            String firstInstanceId,
            int firstCallCount,
            String secondInstanceId,
            int secondCallCount,
            boolean sameInstance,
            String explanation
    ) {
        this.firstInstanceId = firstInstanceId;
        this.firstCallCount = firstCallCount;
        this.secondInstanceId = secondInstanceId;
        this.secondCallCount = secondCallCount;
        this.sameInstance = sameInstance;
        this.explanation = explanation;
    }

    public String getFirstInstanceId() {
        return firstInstanceId;
    }

    public int getFirstCallCount() {
        return firstCallCount;
    }

    public String getSecondInstanceId() {
        return secondInstanceId;
    }

    public int getSecondCallCount() {
        return secondCallCount;
    }

    public boolean isSameInstance() {
        return sameInstance;
    }

    public String getExplanation() {
        return explanation;
    }
}
