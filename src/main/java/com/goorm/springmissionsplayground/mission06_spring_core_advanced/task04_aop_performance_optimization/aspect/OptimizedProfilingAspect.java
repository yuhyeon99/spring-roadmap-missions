package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.OptimizedTrace;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.ExpensivePayloadFormatter;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.OptimizedAspectMetrics;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OptimizedProfilingAspect {

    private static final int SNAPSHOT_ROUNDS = 36;

    private final ExpensivePayloadFormatter expensivePayloadFormatter;
    private final OptimizedAspectMetrics optimizedAspectMetrics;

    public OptimizedProfilingAspect(
            ExpensivePayloadFormatter expensivePayloadFormatter,
            OptimizedAspectMetrics optimizedAspectMetrics
    ) {
        this.expensivePayloadFormatter = expensivePayloadFormatter;
        this.optimizedAspectMetrics = optimizedAspectMetrics;
    }

    @Around("@annotation(optimizedTrace) && args(payload)")
    public Object profile(
            ProceedingJoinPoint joinPoint,
            OptimizedTrace optimizedTrace,
            String payload
    ) throws Throwable {
        long start = System.nanoTime();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodLabel = optimizedAspectMetrics.resolveMethodLabel(method);

        try {
            return joinPoint.proceed();
        } finally {
            long observedNanos = System.nanoTime() - start;
            optimizedAspectMetrics.recordInvocation(observedNanos);

            if (observedNanos >= optimizedTrace.slowThresholdNanos()) {
                String snapshot = expensivePayloadFormatter.createSnapshot(methodLabel, payload, SNAPSHOT_ROUNDS);
                optimizedAspectMetrics.recordSnapshot(snapshot);
            }
        }
    }
}
