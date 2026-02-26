package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto;

public class ScopeComparisonResponse {

    private final ScopePairResult singletonScope;
    private final ScopePairResult prototypeInjectedIntoSingleton;
    private final ScopePairResult prototypeFromProvider;

    public ScopeComparisonResponse(
            ScopePairResult singletonScope,
            ScopePairResult prototypeInjectedIntoSingleton,
            ScopePairResult prototypeFromProvider
    ) {
        this.singletonScope = singletonScope;
        this.prototypeInjectedIntoSingleton = prototypeInjectedIntoSingleton;
        this.prototypeFromProvider = prototypeFromProvider;
    }

    public ScopePairResult getSingletonScope() {
        return singletonScope;
    }

    public ScopePairResult getPrototypeInjectedIntoSingleton() {
        return prototypeInjectedIntoSingleton;
    }

    public ScopePairResult getPrototypeFromProvider() {
        return prototypeFromProvider;
    }
}
