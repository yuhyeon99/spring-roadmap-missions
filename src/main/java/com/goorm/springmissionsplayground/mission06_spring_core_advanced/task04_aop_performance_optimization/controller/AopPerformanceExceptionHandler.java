package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.dto.AopPerformanceErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AopPerformanceOptimizationController.class)
public class AopPerformanceExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public AopPerformanceErrorResponse handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return new AopPerformanceErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                exception.getMessage(),
                request.getRequestURI()
        );
    }
}
