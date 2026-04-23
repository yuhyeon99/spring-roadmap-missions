package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction;

import org.springframework.stereotype.Component;

@Component
public class TransactionAuditStore {

    private volatile TransactionExecutionTrace lastTrace = new TransactionExecutionTrace(
            "N/A",
            "N/A",
            TransactionPhase.NONE,
            false,
            "아직 실행된 트랜잭션이 없습니다.",
            null,
            java.util.List.of()
    );

    public TransactionExecutionTrace getLastTrace() {
        return lastTrace;
    }

    public void save(TransactionExecutionTrace trace) {
        this.lastTrace = trace;
    }
}
