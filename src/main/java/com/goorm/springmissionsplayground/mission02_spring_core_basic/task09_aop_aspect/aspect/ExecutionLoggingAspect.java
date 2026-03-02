package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.aspect;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.annotation.TrackExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionLoggingAspect.class);

    @Around("@annotation(trackExecution)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, TrackExecution trackExecution) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("[TASK09-AOP][{}] executed in {} ms", joinPoint.getSignature().toShortString(), elapsedMs);
        }
    }
}
