package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.OptimizedTrace;
import org.springframework.stereotype.Service;

@Service
public class OptimizedProjectionService {

    private final ProjectionWorkloadExecutor projectionWorkloadExecutor;

    public OptimizedProjectionService(ProjectionWorkloadExecutor projectionWorkloadExecutor) {
        this.projectionWorkloadExecutor = projectionWorkloadExecutor;
    }

    @OptimizedTrace
    public String buildProjection(String payload) {
        return projectionWorkloadExecutor.buildProjection(payload);
    }
}
