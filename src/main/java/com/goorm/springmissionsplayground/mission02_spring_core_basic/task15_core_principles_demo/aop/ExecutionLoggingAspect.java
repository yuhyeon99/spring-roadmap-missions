package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component("task15ExecutionLoggingAspect")
public class ExecutionLoggingAspect {

    private final ExecutionLogStore logStore;

    public ExecutionLoggingAspect(ExecutionLogStore logStore) {
        this.logStore = logStore;
    }

    @Around("@annotation(com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.aop.LogExecution)")
    public Object logExecution(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long took = System.currentTimeMillis() - start;
            logStore.add(pjp.getSignature().toShortString() + " took " + took + "ms");
        }
    }
}
