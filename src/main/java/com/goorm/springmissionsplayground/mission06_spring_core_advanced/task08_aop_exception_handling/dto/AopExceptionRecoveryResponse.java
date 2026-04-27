package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.service.IncidentRecoveryResult;
import java.util.List;

public class AopExceptionRecoveryResponse {

    private final String incidentId;
    private final String operatorId;
    private final String status;
    private final List<String> executedSteps;
    private final int alertCount;

    public AopExceptionRecoveryResponse(
            String incidentId,
            String operatorId,
            String status,
            List<String> executedSteps,
            int alertCount
    ) {
        this.incidentId = incidentId;
        this.operatorId = operatorId;
        this.status = status;
        this.executedSteps = List.copyOf(executedSteps);
        this.alertCount = alertCount;
    }

    public static AopExceptionRecoveryResponse from(IncidentRecoveryResult result, int alertCount) {
        return new AopExceptionRecoveryResponse(
                result.getIncidentId(),
                result.getOperatorId(),
                result.getStatus(),
                result.getExecutedSteps(),
                alertCount
        );
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

    public int getAlertCount() {
        return alertCount;
    }
}
