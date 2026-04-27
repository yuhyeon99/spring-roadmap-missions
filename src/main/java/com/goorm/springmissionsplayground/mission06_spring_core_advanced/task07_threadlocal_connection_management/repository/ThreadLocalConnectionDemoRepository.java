package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.repository;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.DirectConnectionMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionManager;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class ThreadLocalConnectionDemoRepository {

    private final ThreadLocalConnectionManager threadLocalConnectionManager;
    private final DataSource dataSource;

    public ThreadLocalConnectionDemoRepository(
            ThreadLocalConnectionManager threadLocalConnectionManager,
            DataSource dataSource
    ) {
        this.threadLocalConnectionManager = threadLocalConnectionManager;
        this.dataSource = dataSource;
    }

    public ThreadLocalConnectionVisit loadPlanSummary(String planId) {
        return threadLocalConnectionManager.withCurrentConnection(connection -> createVisit("loadPlanSummary", connection));
    }

    public ThreadLocalConnectionVisit loadApprovalHistory(String planId) {
        return threadLocalConnectionManager.withCurrentConnection(connection -> createVisit("loadApprovalHistory", connection));
    }

    public String loadPlanSummaryDirect(String planId, DirectConnectionMetrics directConnectionMetrics) {
        directConnectionMetrics.recordOpen();
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName() + ":" + planId;
        } catch (Exception exception) {
            throw new IllegalStateException("직접 연결 plan summary 조회에 실패했습니다.", exception);
        }
    }

    public String loadApprovalHistoryDirect(String planId, DirectConnectionMetrics directConnectionMetrics) {
        directConnectionMetrics.recordOpen();
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getDatabaseProductName() + ":" + planId;
        } catch (Exception exception) {
            throw new IllegalStateException("직접 연결 approval history 조회에 실패했습니다.", exception);
        }
    }

    private ThreadLocalConnectionVisit createVisit(String repositoryMethod, Connection connection) throws SQLException {
        return new ThreadLocalConnectionVisit(
                repositoryMethod,
                Thread.currentThread().getName(),
                threadLocalConnectionManager.currentSnapshot().getConnectionId(),
                connection.getMetaData().getDatabaseProductName()
        );
    }
}
