package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class ThreadLocalConnectionManager {

    private final DataSource dataSource;
    private final ThreadLocalConnectionAuditStore auditStore;
    private final ThreadLocal<ManagedConnectionContext> threadBoundConnection = new ThreadLocal<>();
    private final AtomicInteger connectionSequence = new AtomicInteger(1);
    private final AtomicInteger openedConnectionCount = new AtomicInteger();

    public ThreadLocalConnectionManager(DataSource dataSource, ThreadLocalConnectionAuditStore auditStore) {
        this.dataSource = dataSource;
        this.auditStore = auditStore;
    }

    public void resetOpenedConnectionCount() {
        openedConnectionCount.set(0);
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount.get();
    }

    public <T> T executeInSession(String sessionLabel, ConnectionCallback<T> callback) {
        ManagedConnectionContext existingContext = threadBoundConnection.get();
        if (existingContext != null) {
            existingContext.incrementNestingLevel();
            auditStore.add(
                    "REUSE",
                    "기존 연결 재사용 - thread=" + existingContext.threadName
                            + ", connectionId=" + existingContext.connectionId
                            + ", nestingLevel=" + existingContext.nestingLevel
            );
            try {
                return callback.doInConnection(existingContext.connection);
            } catch (SQLException exception) {
                throw new IllegalStateException("ThreadLocal 연결 재사용 작업에 실패했습니다.", exception);
            } finally {
                existingContext.decrementNestingLevel();
            }
        }

        Connection connection = openConnection();
        ManagedConnectionContext newContext = new ManagedConnectionContext(
                connectionSequence.getAndIncrement(),
                connection,
                Thread.currentThread().getName(),
                sessionLabel
        );
        threadBoundConnection.set(newContext);
        openedConnectionCount.incrementAndGet();
        auditStore.add(
                "ACQUIRE",
                "새 연결 생성 - thread=" + newContext.threadName
                        + ", connectionId=" + newContext.connectionId
                        + ", sessionLabel=" + sessionLabel
        );

        try {
            return callback.doInConnection(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("ThreadLocal 세션 작업에 실패했습니다.", exception);
        } finally {
            closeConnection(newContext);
        }
    }

    public <T> T withCurrentConnection(ConnectionCallback<T> callback) {
        ManagedConnectionContext context = requireContext();
        try {
            return callback.doInConnection(context.connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("현재 ThreadLocal 연결 작업에 실패했습니다.", exception);
        }
    }

    public ThreadLocalSessionSnapshot currentSnapshot() {
        ManagedConnectionContext context = requireContext();
        return new ThreadLocalSessionSnapshot(
                context.connectionId,
                context.threadName,
                context.nestingLevel,
                context.sessionLabel
        );
    }

    private ManagedConnectionContext requireContext() {
        ManagedConnectionContext context = threadBoundConnection.get();
        if (context == null) {
            throw new IllegalStateException("현재 스레드에 바인딩된 DB 연결이 없습니다.");
        }
        return context;
    }

    private Connection openConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException("DB 연결 획득에 실패했습니다.", exception);
        }
    }

    private void closeConnection(ManagedConnectionContext context) {
        try {
            context.connection.close();
            auditStore.add(
                    "RELEASE",
                    "연결 해제 - thread=" + context.threadName
                            + ", connectionId=" + context.connectionId
            );
        } catch (SQLException exception) {
            throw new IllegalStateException("DB 연결 해제에 실패했습니다.", exception);
        } finally {
            threadBoundConnection.remove();
        }
    }

    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T doInConnection(Connection connection) throws SQLException;
    }

    private static final class ManagedConnectionContext {

        private final int connectionId;
        private final Connection connection;
        private final String threadName;
        private final String sessionLabel;
        private int nestingLevel;

        private ManagedConnectionContext(
                int connectionId,
                Connection connection,
                String threadName,
                String sessionLabel
        ) {
            this.connectionId = connectionId;
            this.connection = connection;
            this.threadName = threadName;
            this.sessionLabel = sessionLabel;
            this.nestingLevel = 1;
        }

        private void incrementNestingLevel() {
            nestingLevel++;
        }

        private void decrementNestingLevel() {
            nestingLevel--;
        }
    }
}
