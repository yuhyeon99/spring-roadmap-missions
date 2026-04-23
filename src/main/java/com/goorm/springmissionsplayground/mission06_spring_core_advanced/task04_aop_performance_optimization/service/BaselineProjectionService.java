package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.BaselineTrace;
import org.springframework.stereotype.Service;

@Service
public class BaselineProjectionService {

    private final ProjectionWorkloadExecutor projectionWorkloadExecutor;

    public BaselineProjectionService(ProjectionWorkloadExecutor projectionWorkloadExecutor) {
        this.projectionWorkloadExecutor = projectionWorkloadExecutor;
    }

    @BaselineTrace
    public String buildProjection(String payload) {
        return projectionWorkloadExecutor.buildProjection(payload);
    }
}
