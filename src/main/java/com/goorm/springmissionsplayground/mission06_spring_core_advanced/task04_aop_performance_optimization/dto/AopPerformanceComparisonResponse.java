package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto;

import java.util.List;

public class AopPerformanceComparisonResponse {

    private final int iterations;
    private final int payloadSize;
    private final long baselineElapsedNanos;
    private final long optimizedElapsedNanos;
    private final double baselinePerCallMicros;
    private final double optimizedPerCallMicros;
    private final double improvementPercent;
    private final long baselineInvocationCount;
    private final long baselineSnapshotCount;
    private final long optimizedInvocationCount;
    private final long optimizedSnapshotCount;
    private final long optimizedMetadataCacheHitCount;
    private final long optimizedMetadataCacheMissCount;
    private final boolean resultEquality;
    private final String projectionPreview;
    private final List<String> optimizationStrategies;

    public AopPerformanceComparisonResponse(
            int iterations,
            int payloadSize,
            long baselineElapsedNanos,
            long optimizedElapsedNanos,
            long baselineInvocationCount,
            long baselineSnapshotCount,
            long optimizedInvocationCount,
            long optimizedSnapshotCount,
            long optimizedMetadataCacheHitCount,
            long optimizedMetadataCacheMissCount,
            boolean resultEquality,
            String projectionPreview,
            List<String> optimizationStrategies
    ) {
        this.iterations = iterations;
        this.payloadSize = payloadSize;
        this.baselineElapsedNanos = baselineElapsedNanos;
        this.optimizedElapsedNanos = optimizedElapsedNanos;
        this.baselinePerCallMicros = nanosToMicros(baselineElapsedNanos, iterations);
        this.optimizedPerCallMicros = nanosToMicros(optimizedElapsedNanos, iterations);
        this.improvementPercent = calculateImprovementPercent(baselineElapsedNanos, optimizedElapsedNanos);
        this.baselineInvocationCount = baselineInvocationCount;
        this.baselineSnapshotCount = baselineSnapshotCount;
        this.optimizedInvocationCount = optimizedInvocationCount;
        this.optimizedSnapshotCount = optimizedSnapshotCount;
        this.optimizedMetadataCacheHitCount = optimizedMetadataCacheHitCount;
        this.optimizedMetadataCacheMissCount = optimizedMetadataCacheMissCount;
        this.resultEquality = resultEquality;
        this.projectionPreview = projectionPreview;
        this.optimizationStrategies = List.copyOf(optimizationStrategies);
    }

    private double nanosToMicros(long nanos, int iterations) {
        return nanos / (double) iterations / 1_000.0;
    }

    private double calculateImprovementPercent(long baseline, long optimized) {
        if (baseline <= 0L) {
            return 0.0;
        }
        return ((baseline - optimized) / (double) baseline) * 100.0;
    }

    public int getIterations() {
        return iterations;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public long getBaselineElapsedNanos() {
        return baselineElapsedNanos;
    }

    public long getOptimizedElapsedNanos() {
        return optimizedElapsedNanos;
    }

    public double getBaselinePerCallMicros() {
        return baselinePerCallMicros;
    }

    public double getOptimizedPerCallMicros() {
        return optimizedPerCallMicros;
    }

    public double getImprovementPercent() {
        return improvementPercent;
    }

    public long getBaselineInvocationCount() {
        return baselineInvocationCount;
    }

    public long getBaselineSnapshotCount() {
        return baselineSnapshotCount;
    }

    public long getOptimizedInvocationCount() {
        return optimizedInvocationCount;
    }

    public long getOptimizedSnapshotCount() {
        return optimizedSnapshotCount;
    }

    public long getOptimizedMetadataCacheHitCount() {
        return optimizedMetadataCacheHitCount;
    }

    public long getOptimizedMetadataCacheMissCount() {
        return optimizedMetadataCacheMissCount;
    }

    public boolean isResultEquality() {
        return resultEquality;
    }

    public String getProjectionPreview() {
        return projectionPreview;
    }

    public List<String> getOptimizationStrategies() {
        return optimizationStrategies;
    }
}
