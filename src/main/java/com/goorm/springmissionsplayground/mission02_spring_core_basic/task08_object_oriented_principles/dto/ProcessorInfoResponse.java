package com.goorm.springmissionsplayground.mission02_spring_core_basic.task08_object_oriented_principles.dto;

import java.util.Map;

public class ProcessorInfoResponse {

    private final Map<String, String> methodToBean;
    private final String polymorphismSummary;

    public ProcessorInfoResponse(Map<String, String> methodToBean, String polymorphismSummary) {
        this.methodToBean = methodToBean;
        this.polymorphismSummary = polymorphismSummary;
    }

    public Map<String, String> getMethodToBean() {
        return methodToBean;
    }

    public String getPolymorphismSummary() {
        return polymorphismSummary;
    }
}
