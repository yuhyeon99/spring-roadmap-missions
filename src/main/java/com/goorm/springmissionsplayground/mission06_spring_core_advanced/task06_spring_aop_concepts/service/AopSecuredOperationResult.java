package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.domain.AopUserRole;

public class AopSecuredOperationResult {

    private final String planId;
    private final String action;
    private final String operatorId;
    private final AopUserRole currentRole;
    private final AopUserRole requiredRole;
    private final String resultMessage;

    public AopSecuredOperationResult(
            String planId,
            String action,
            String operatorId,
            AopUserRole currentRole,
            AopUserRole requiredRole,
            String resultMessage
    ) {
        this.planId = planId;
        this.action = action;
        this.operatorId = operatorId;
        this.currentRole = currentRole;
        this.requiredRole = requiredRole;
        this.resultMessage = resultMessage;
    }

    public String getPlanId() {
        return planId;
    }

    public String getAction() {
        return action;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public AopUserRole getCurrentRole() {
        return currentRole;
    }

    public AopUserRole getRequiredRole() {
        return requiredRole;
    }

    public String getResultMessage() {
        return resultMessage;
    }
}
