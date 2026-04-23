package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class ConsoleTransactionManager {

    private static final AtomicLong SEQUENCE = new AtomicLong(1000);

    private final TransactionAuditStore transactionAuditStore;

    public ConsoleTransactionManager(TransactionAuditStore transactionAuditStore) {
        this.transactionAuditStore = transactionAuditStore;
    }

    public TransactionContext begin(String methodName) {
        String transactionId = "TX-" + SEQUENCE.incrementAndGet();
        TransactionContext context = new TransactionContext(transactionId, methodName);

        appendEvent(context, "[TX-BEGIN] id=%s method=%s".formatted(transactionId, methodName));
        inspect(context, TransactionPhase.ACTIVE, true, "트랜잭션 시작 직후 상태 점검", null);
        return context;
    }

    public void inspect(
            TransactionContext context,
            TransactionPhase phase,
            boolean active,
            String detail,
            String failureReason
    ) {
        appendEvent(
                context,
                "[TX-STATUS] id=%s phase=%s active=%s detail=%s".formatted(
                        context.getTransactionId(),
                        phase.name(),
                        active,
                        detail
                )
        );

        transactionAuditStore.save(new TransactionExecutionTrace(
                context.getTransactionId(),
                context.getMethodName(),
                phase,
                active,
                detail,
                failureReason,
                context.getEvents()
        ));
    }

    public void commit(TransactionContext context) {
        appendEvent(context, "[TX-COMMIT] id=%s message=정상 커밋 완료".formatted(context.getTransactionId()));
        inspect(context, TransactionPhase.COMMITTED, false, "커밋 후 상태 점검", null);
    }

    public void rollback(TransactionContext context, Throwable throwable) {
        String failureReason = throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
        appendEvent(
                context,
                "[TX-ROLLBACK] id=%s reason=%s".formatted(context.getTransactionId(), failureReason)
        );
        inspect(context, TransactionPhase.ROLLED_BACK, false, "롤백 후 상태 점검", failureReason);
    }

    public TransactionExecutionTrace getLastTrace() {
        return transactionAuditStore.getLastTrace();
    }

    private void appendEvent(TransactionContext context, String event) {
        context.getMutableEvents().add(event);
        System.out.println(event);
    }

    public static class TransactionContext {

        private final String transactionId;
        private final String methodName;
        private final List<String> events = new ArrayList<>();

        public TransactionContext(String transactionId, String methodName) {
            this.transactionId = transactionId;
            this.methodName = methodName;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<String> getEvents() {
            return List.copyOf(events);
        }

        public List<String> getMutableEvents() {
            return events;
        }
    }
}
