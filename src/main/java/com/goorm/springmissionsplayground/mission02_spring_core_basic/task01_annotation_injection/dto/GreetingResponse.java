package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto;

public class GreetingResponse {

    private final String message;
    private final String selectedPolicy;
    private final String injectionType;

    public GreetingResponse(String message, String selectedPolicy, String injectionType) {
        this.message = message;
        this.selectedPolicy = selectedPolicy;
        this.injectionType = injectionType;
    }

    public String getMessage() {
        return message;
    }

    public String getSelectedPolicy() {
        return selectedPolicy;
    }

    public String getInjectionType() {
        return injectionType;
    }
}
