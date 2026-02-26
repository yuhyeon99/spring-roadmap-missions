package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto;

public class InjectionCaseResult {

    private final String injectionType;
    private final String injectedBean;
    private final String result;
    private final String reason;

    public InjectionCaseResult(
            String injectionType,
            String injectedBean,
            String result,
            String reason
    ) {
        this.injectionType = injectionType;
        this.injectedBean = injectedBean;
        this.result = result;
        this.reason = reason;
    }

    public String getInjectionType() {
        return injectionType;
    }

    public String getInjectedBean() {
        return injectedBean;
    }

    public String getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }
}
