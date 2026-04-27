package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.service.AopSecuredOperationResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.support.AopAccessAuditEntry;
import java.util.List;

public class AopSecurityOperationResponse {

    private final String planId;
    private final String action;
    private final String operatorId;
    private final String currentRole;
    private final String requiredRole;
    private final String resultMessage;
    private final boolean aopApplied;
    private final List<AopAccessAuditEntry> auditTrail;

    public AopSecurityOperationResponse(
            String planId,
            String action,
            String operatorId,
            String currentRole,
            String requiredRole,
            String resultMessage,
            boolean aopApplied,
            List<AopAccessAuditEntry> auditTrail
    ) {
        this.planId = planId;
        this.action = action;
        this.operatorId = operatorId;
        this.currentRole = currentRole;
        this.requiredRole = requiredRole;
        this.resultMessage = resultMessage;
        this.aopApplied = aopApplied;
        this.auditTrail = List.copyOf(auditTrail);
    }

    public static AopSecurityOperationResponse from(
            AopSecuredOperationResult result,
            List<AopAccessAuditEntry> auditTrail
    ) {
        return new AopSecurityOperationResponse(
                result.getPlanId(),
                result.getAction(),
                result.getOperatorId(),
                result.getCurrentRole().name(),
                result.getRequiredRole().name(),
                result.getResultMessage(),
                true,
                auditTrail
        );
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

    public String getCurrentRole() {
        return currentRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public boolean isAopApplied() {
        return aopApplied;
    }

    public List<AopAccessAuditEntry> getAuditTrail() {
        return auditTrail;
    }
}
