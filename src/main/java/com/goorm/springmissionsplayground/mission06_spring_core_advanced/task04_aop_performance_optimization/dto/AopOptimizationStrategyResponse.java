package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto;

import java.util.List;

public class AopOptimizationStrategyResponse {

    private final String missionGoal;
    private final List<String> strategies;

    public AopOptimizationStrategyResponse(String missionGoal, List<String> strategies) {
        this.missionGoal = missionGoal;
        this.strategies = List.copyOf(strategies);
    }

    public String getMissionGoal() {
        return missionGoal;
    }

    public List<String> getStrategies() {
        return strategies;
    }
}
