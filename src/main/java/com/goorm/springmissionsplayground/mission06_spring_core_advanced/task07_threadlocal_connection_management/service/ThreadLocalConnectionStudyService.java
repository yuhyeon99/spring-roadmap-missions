package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.repository.ThreadLocalConnectionDemoRepository;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.DirectConnectionMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionAuditStore;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionManager;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalConnectionVisit;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support.ThreadLocalSessionSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.stereotype.Service;

@Service
public class ThreadLocalConnectionStudyService {

    private final ThreadLocalConnectionManager threadLocalConnectionManager;
    private final ThreadLocalConnectionAuditStore threadLocalConnectionAuditStore;
    private final ThreadLocalConnectionDemoRepository threadLocalConnectionDemoRepository;
    private final DirectConnectionMetrics directConnectionMetrics;

    public ThreadLocalConnectionStudyService(
            ThreadLocalConnectionManager threadLocalConnectionManager,
            ThreadLocalConnectionAuditStore threadLocalConnectionAuditStore,
            ThreadLocalConnectionDemoRepository threadLocalConnectionDemoRepository,
            DirectConnectionMetrics directConnectionMetrics
    ) {
        this.threadLocalConnectionManager = threadLocalConnectionManager;
        this.threadLocalConnectionAuditStore = threadLocalConnectionAuditStore;
        this.threadLocalConnectionDemoRepository = threadLocalConnectionDemoRepository;
        this.directConnectionMetrics = directConnectionMetrics;
    }

    public ThreadLocalConnectionDemoResult demonstrateThreadBoundConnection(String planId, String operatorId) {
        validatePlanId(planId);
        validateOperatorId(operatorId);

        threadLocalConnectionAuditStore.reset();
        threadLocalConnectionManager.resetOpenedConnectionCount();

        DemoCapture capture = threadLocalConnectionManager.executeInSession("deployment-plan-demo", connection -> {
            List<ThreadLocalConnectionVisit> visits = new ArrayList<>();
            visits.add(threadLocalConnectionDemoRepository.loadPlanSummary(planId));
            visits.add(threadLocalConnectionDemoRepository.loadApprovalHistory(planId));

            ThreadLocalSessionSnapshot snapshot = threadLocalConnectionManager.currentSnapshot();

            return new DemoCapture(snapshot.getThreadName(), snapshot.getConnectionId(), visits);
        });

        return new ThreadLocalConnectionDemoResult(
                planId,
                operatorId,
                capture.threadName(),
                capture.connectionId(),
                threadLocalConnectionManager.getOpenedConnectionCount(),
                "같은 스레드에서 plan summary와 approval history 조회가 같은 DB 연결을 재사용했습니다.",
                capture.repositoryVisits(),
                threadLocalConnectionAuditStore.getEntries()
        );
    }

    public ThreadLocalConnectionPerformanceResult measurePerformance(int workerCount, int iterationsPerWorker) {
        validateBenchmarkInput(workerCount, iterationsPerWorker);

        BenchmarkOutcome directOutcome = benchmarkDirect(workerCount, iterationsPerWorker);
        BenchmarkOutcome threadLocalOutcome = benchmarkThreadLocal(workerCount, iterationsPerWorker);

        return new ThreadLocalConnectionPerformanceResult(
                workerCount,
                iterationsPerWorker,
                directOutcome.elapsedMs(),
                threadLocalOutcome.elapsedMs(),
                directOutcome.connectionAcquisitions(),
                threadLocalOutcome.connectionAcquisitions(),
                directOutcome.connectionAcquisitions() - threadLocalOutcome.connectionAcquisitions(),
                List.of(
                        "직접 획득 방식은 repository 호출마다 새 Connection을 열고 닫습니다.",
                        "ThreadLocal 방식은 같은 스레드 안에서 하나의 Connection을 재사용합니다.",
                        "실행 시간은 환경에 따라 달라질 수 있지만, 연결 획득 횟수 차이는 안정적으로 확인할 수 있습니다."
                )
        );
    }

    private BenchmarkOutcome benchmarkDirect(int workerCount, int iterationsPerWorker) {
        directConnectionMetrics.reset();
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
        long start = System.nanoTime();

        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
                final int workerId = workerIndex;
                futures.add(executorService.submit(runDirectScenario(workerId, iterationsPerWorker)));
            }
            waitForAll(futures);
        } finally {
            executorService.shutdownNow();
        }

        return new BenchmarkOutcome((System.nanoTime() - start) / 1_000_000, directConnectionMetrics.getOpenedConnectionCount());
    }

    private BenchmarkOutcome benchmarkThreadLocal(int workerCount, int iterationsPerWorker) {
        threadLocalConnectionManager.resetOpenedConnectionCount();
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
        long start = System.nanoTime();

        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (int workerIndex = 0; workerIndex < workerCount; workerIndex++) {
                final int workerId = workerIndex;
                futures.add(executorService.submit(runThreadLocalScenario(workerId, iterationsPerWorker)));
            }
            waitForAll(futures);
        } finally {
            executorService.shutdownNow();
        }

        return new BenchmarkOutcome((System.nanoTime() - start) / 1_000_000, threadLocalConnectionManager.getOpenedConnectionCount());
    }

    private Callable<Void> runDirectScenario(int workerId, int iterationsPerWorker) {
        return () -> {
            for (int iteration = 0; iteration < iterationsPerWorker; iteration++) {
                String planId = "direct-plan-" + workerId + "-" + iteration;
                threadLocalConnectionDemoRepository.loadPlanSummaryDirect(planId, directConnectionMetrics);
                threadLocalConnectionDemoRepository.loadApprovalHistoryDirect(planId, directConnectionMetrics);
            }
            return null;
        };
    }

    private Callable<Void> runThreadLocalScenario(int workerId, int iterationsPerWorker) {
        return () -> {
            threadLocalConnectionManager.executeInSession("worker-" + workerId, connection -> {
                for (int iteration = 0; iteration < iterationsPerWorker; iteration++) {
                    String planId = "threadlocal-plan-" + workerId + "-" + iteration;
                    threadLocalConnectionDemoRepository.loadPlanSummary(planId);
                    threadLocalConnectionDemoRepository.loadApprovalHistory(planId);
                }
                return null;
            });
            return null;
        };
    }

    private void waitForAll(List<Future<Void>> futures) {
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("성능 측정 스레드가 인터럽트되었습니다.", exception);
            } catch (ExecutionException exception) {
                throw new IllegalStateException("성능 측정 작업이 실패했습니다.", exception.getCause());
            }
        }
    }

    private void validatePlanId(String planId) {
        if (planId == null || planId.isBlank()) {
            throw new IllegalArgumentException("planId는 비어 있을 수 없습니다.");
        }
    }

    private void validateOperatorId(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw new IllegalArgumentException("operatorId는 비어 있을 수 없습니다.");
        }
    }

    private void validateBenchmarkInput(int workerCount, int iterationsPerWorker) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount는 1 이상이어야 합니다.");
        }
        if (iterationsPerWorker <= 0) {
            throw new IllegalArgumentException("iterationsPerWorker는 1 이상이어야 합니다.");
        }
    }

    private record BenchmarkOutcome(long elapsedMs, int connectionAcquisitions) {
    }

    private record DemoCapture(
            String threadName,
            int connectionId,
            List<ThreadLocalConnectionVisit> repositoryVisits
    ) {
    }
}
