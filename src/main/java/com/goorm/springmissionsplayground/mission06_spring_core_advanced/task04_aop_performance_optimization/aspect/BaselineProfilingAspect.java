package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.BaselineAspectMetrics;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support.ExpensivePayloadFormatter;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BaselineProfilingAspect {

    private static final int SNAPSHOT_ROUNDS = 36;

    private final ExpensivePayloadFormatter expensivePayloadFormatter;
    private final BaselineAspectMetrics baselineAspectMetrics;

    public BaselineProfilingAspect(
            ExpensivePayloadFormatter expensivePayloadFormatter,
            BaselineAspectMetrics baselineAspectMetrics
    ) {
        this.expensivePayloadFormatter = expensivePayloadFormatter;
        this.baselineAspectMetrics = baselineAspectMetrics;
    }

    @Around("@annotation(com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.annotation.BaselineTrace) && args(payload)")
    public Object profile(ProceedingJoinPoint joinPoint, String payload) throws Throwable {
        long start = System.nanoTime();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String methodLabel = buildVerboseMethodLabel(method);
        String snapshot = expensivePayloadFormatter.createSnapshot(methodLabel, payload, SNAPSHOT_ROUNDS);
        baselineAspectMetrics.recordSnapshot(snapshot);

        try {
            return joinPoint.proceed();
        } finally {
            baselineAspectMetrics.recordInvocation(System.nanoTime() - start);
        }
    }

    private String buildVerboseMethodLabel(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName())
                .append("#")
                .append(method.getName())
                .append("(");

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int index = 0; index < parameterTypes.length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(parameterTypes[index].getTypeName());
        }

        builder.append(")");
        return builder.toString();
    }
}
