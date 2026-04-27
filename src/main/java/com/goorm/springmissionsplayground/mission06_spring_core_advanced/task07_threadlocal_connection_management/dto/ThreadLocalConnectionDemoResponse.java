package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionDemoResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionAuditEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import java.util.List;

public class ThreadLocalConnectionDemoResponse {

    private final String planId;
    private final String operatorId;
    private final String threadName;
    private final int connectionId;
    private final int openedConnectionCount;
    private final String resultMessage;
    private final List<ThreadLocalConnectionVisit> repositoryVisits;
    private final List<ThreadLocalConnectionAuditEntry> auditTrail;

    public ThreadLocalConnectionDemoResponse(
            String planId,
            String operatorId,
            String threadName,
            int connectionId,
            int openedConnectionCount,
            String resultMessage,
            List<ThreadLocalConnectionVisit> repositoryVisits,
            List<ThreadLocalConnectionAuditEntry> auditTrail
    ) {
        this.planId = planId;
        this.operatorId = operatorId;
        this.threadName = threadName;
        this.connectionId = connectionId;
        this.openedConnectionCount = openedConnectionCount;
        this.resultMessage = resultMessage;
        this.repositoryVisits = List.copyOf(repositoryVisits);
        this.auditTrail = List.copyOf(auditTrail);
    }

    public static ThreadLocalConnectionDemoResponse from(ThreadLocalConnectionDemoResult result) {
        return new ThreadLocalConnectionDemoResponse(
                result.getPlanId(),
                result.getOperatorId(),
                result.getThreadName(),
                result.getConnectionId(),
                result.getOpenedConnectionCount(),
                result.getResultMessage(),
                result.getRepositoryVisits(),
                result.getAuditTrail()
        );
    }

    public String getPlanId() {
        return planId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public List<ThreadLocalConnectionVisit> getRepositoryVisits() {
        return repositoryVisits;
    }

    public List<ThreadLocalConnectionAuditEntry> getAuditTrail() {
        return auditTrail;
    }
}
