package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain;

public class AopSecurityRequest {

    private final String operatorId;
    private final AopUserRole role;

    public AopSecurityRequest(String operatorId, AopUserRole role) {
        this.operatorId = operatorId;
        this.role = role;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public AopUserRole getRole() {
        return role;
    }
}
