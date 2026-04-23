package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopOptimizationStrategyResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceComparisonResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.service.AopPerformanceComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task04/aop-performance")
public class AopPerformanceOptimizationController {

    private final AopPerformanceComparisonService aopPerformanceComparisonService;

    public AopPerformanceOptimizationController(AopPerformanceComparisonService aopPerformanceComparisonService) {
        this.aopPerformanceComparisonService = aopPerformanceComparisonService;
    }

    @GetMapping("/strategies")
    public AopOptimizationStrategyResponse strategies() {
        return aopPerformanceComparisonService.describeStrategies();
    }

    @GetMapping("/compare")
    public AopPerformanceComparisonResponse compare(
            @RequestParam(defaultValue = "120") int iterations,
            @RequestParam(defaultValue = "480") int payloadSize
    ) {
        return aopPerformanceComparisonService.compare(iterations, payloadSize);
    }
}
