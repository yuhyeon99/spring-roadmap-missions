package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction;

import java.util.List;

public class TransactionExecutionTrace {

    private final String transactionId;
    private final String methodName;
    private final TransactionPhase phase;
    private final boolean active;
    private final String detail;
    private final String failureReason;
    private final List<String> events;

    public TransactionExecutionTrace(
            String transactionId,
            String methodName,
            TransactionPhase phase,
            boolean active,
            String detail,
            String failureReason,
            List<String> events
    ) {
        this.transactionId = transactionId;
        this.methodName = methodName;
        this.phase = phase;
        this.active = active;
        this.detail = detail;
        this.failureReason = failureReason;
        this.events = List.copyOf(events);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMethodName() {
        return methodName;
    }

    public TransactionPhase getPhase() {
        return phase;
    }

    public boolean isActive() {
        return active;
    }

    public String getDetail() {
        return detail;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public List<String> getEvents() {
        return events;
    }
}
