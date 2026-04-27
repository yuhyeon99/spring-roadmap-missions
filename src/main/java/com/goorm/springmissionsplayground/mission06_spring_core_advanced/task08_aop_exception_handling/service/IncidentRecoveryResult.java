package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service;

import java.util.List;

public class IncidentRecoveryResult {

    private final String incidentId;
    private final String operatorId;
    private final String status;
    private final List<String> executedSteps;

    public IncidentRecoveryResult(
            String incidentId,
            String operatorId,
            String status,
            List<String> executedSteps
    ) {
        this.incidentId = incidentId;
        this.operatorId = operatorId;
        this.status = status;
        this.executedSteps = List.copyOf(executedSteps);
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getExecutedSteps() {
        return executedSteps;
    }
}
