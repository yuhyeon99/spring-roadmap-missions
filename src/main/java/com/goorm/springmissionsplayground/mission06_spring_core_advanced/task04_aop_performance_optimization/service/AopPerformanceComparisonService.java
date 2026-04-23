package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopOptimizationStrategyResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceComparisonResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.BaselineAspectMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.OptimizedAspectMetrics;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AopPerformanceComparisonService {

    private static final List<String> OPTIMIZATION_STRATEGIES = List.of(
            "포인트컷을 @OptimizedTrace 메서드로 좁혀 비교 대상 메서드만 추적합니다.",
            "메서드 라벨은 ConcurrentHashMap에 캐시해 매 호출마다 리플렉션 문자열 조합을 반복하지 않습니다.",
            "비용이 큰 payload 스냅샷은 느린 호출에서만 생성해 일반 호출의 AOP 부하를 줄입니다."
    );

    private final BaselineProjectionService baselineProjectionService;
    private final OptimizedProjectionService optimizedProjectionService;
    private final BaselineAspectMetrics baselineAspectMetrics;
    private final OptimizedAspectMetrics optimizedAspectMetrics;

    public AopPerformanceComparisonService(
            BaselineProjectionService baselineProjectionService,
            OptimizedProjectionService optimizedProjectionService,
            BaselineAspectMetrics baselineAspectMetrics,
            OptimizedAspectMetrics optimizedAspectMetrics
    ) {
        this.baselineProjectionService = baselineProjectionService;
        this.optimizedProjectionService = optimizedProjectionService;
        this.baselineAspectMetrics = baselineAspectMetrics;
        this.optimizedAspectMetrics = optimizedAspectMetrics;
    }

    public AopOptimizationStrategyResponse describeStrategies() {
        return new AopOptimizationStrategyResponse(
                "AOP 전후 부가 기능은 유지하되, 반복 리플렉션과 불필요한 스냅샷 생성을 줄여 성능 저하를 완화합니다.",
                OPTIMIZATION_STRATEGIES
        );
    }

    public AopPerformanceComparisonResponse compare(int iterations, int payloadSize) {
        validate(iterations, payloadSize);

        String payload = createPayload(payloadSize);
        warmUp(payload, Math.max(8, Math.min(20, iterations / 5)));
        baselineAspectMetrics.reset();
        optimizedAspectMetrics.reset();

        BenchmarkOutcome baselineOutcome = benchmark(iterations, payload, baselineProjectionService::buildProjection);
        BenchmarkOutcome optimizedOutcome = benchmark(iterations, payload, optimizedProjectionService::buildProjection);

        return new AopPerformanceComparisonResponse(
                iterations,
                payloadSize,
                baselineOutcome.elapsedNanos(),
                optimizedOutcome.elapsedNanos(),
                baselineAspectMetrics.invocationCount(),
                baselineAspectMetrics.snapshotCount(),
                optimizedAspectMetrics.invocationCount(),
                optimizedAspectMetrics.snapshotCount(),
                optimizedAspectMetrics.metadataCacheHitCount(),
                optimizedAspectMetrics.metadataCacheMissCount(),
                baselineOutcome.lastProjection().equals(optimizedOutcome.lastProjection()),
                optimizedOutcome.lastProjection(),
                OPTIMIZATION_STRATEGIES
        );
    }

    private void validate(int iterations, int payloadSize) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("iterations는 1 이상이어야 합니다.");
        }
        if (payloadSize < 64) {
            throw new IllegalArgumentException("payloadSize는 64 이상이어야 합니다.");
        }
    }

    private void warmUp(String payload, int warmupIterations) {
        for (int index = 0; index < warmupIterations; index++) {
            baselineProjectionService.buildProjection(payload);
            optimizedProjectionService.buildProjection(payload);
        }
    }

    private BenchmarkOutcome benchmark(int iterations, String payload, ProjectionRunner projectionRunner) {
        long start = System.nanoTime();
        String lastProjection = "";

        for (int index = 0; index < iterations; index++) {
            lastProjection = projectionRunner.run(payload);
        }

        return new BenchmarkOutcome(System.nanoTime() - start, lastProjection);
    }

    private String createPayload(int payloadSize) {
        String seed = "mission06-aop-performance-optimization-";
        StringBuilder builder = new StringBuilder(payloadSize);
        while (builder.length() < payloadSize) {
            builder.append(seed);
        }
        return builder.substring(0, payloadSize);
    }

    @FunctionalInterface
    private interface ProjectionRunner {
        String run(String payload);
    }

    private record BenchmarkOutcome(long elapsedNanos, String lastProjection) {
    }
}
