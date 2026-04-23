package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task01_proxy_security.dto;

import java.util.List;

public class ProjectOperationPayload {

    private final String projectId;
    private final String action;
    private final String resultMessage;
    private final String requestedBy;
    private final String requestedRole;
    private final String requiredRole;
    private final boolean proxyApplied;
    private final List<String> securityChecks;

    public ProjectOperationPayload(String projectId, String action, String resultMessage) {
        this(projectId, action, resultMessage, null, null, null, false, List.of());
    }

    private ProjectOperationPayload(
            String projectId,
            String action,
            String resultMessage,
            String requestedBy,
            String requestedRole,
            String requiredRole,
            boolean proxyApplied,
            List<String> securityChecks
    ) {
        this.projectId = projectId;
        this.action = action;
        this.resultMessage = resultMessage;
        this.requestedBy = requestedBy;
        this.requestedRole = requestedRole;
        this.requiredRole = requiredRole;
        this.proxyApplied = proxyApplied;
        this.securityChecks = List.copyOf(securityChecks);
    }

    public ProjectOperationPayload withSecurityMetadata(
            String requestedBy,
            String requestedRole,
            String requiredRole,
            boolean proxyApplied,
            List<String> securityChecks
    ) {
        return new ProjectOperationPayload(
                projectId,
                action,
                resultMessage,
                requestedBy,
                requestedRole,
                requiredRole,
                proxyApplied,
                securityChecks
        );
    }

    public String getProjectId() {
        return projectId;
    }

    public String getAction() {
        return action;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public String getRequestedRole() {
        return requestedRole;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public boolean isProxyApplied() {
        return proxyApplied;
    }

    public List<String> getSecurityChecks() {
        return securityChecks;
    }
}
