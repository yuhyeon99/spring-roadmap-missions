package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction.TransactionExecutionTrace;
import java.util.List;

public class TransactionSnapshotResponse {

    private final String transactionId;
    private final String methodName;
    private final String phase;
    private final boolean active;
    private final String detail;
    private final String failureReason;
    private final List<String> events;

    public TransactionSnapshotResponse(
            String transactionId,
            String methodName,
            String phase,
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

    public static TransactionSnapshotResponse from(TransactionExecutionTrace trace) {
        return new TransactionSnapshotResponse(
                trace.getTransactionId(),
                trace.getMethodName(),
                trace.getPhase().name(),
                trace.isActive(),
                trace.getDetail(),
                trace.getFailureReason(),
                trace.getEvents()
        );
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPhase() {
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
