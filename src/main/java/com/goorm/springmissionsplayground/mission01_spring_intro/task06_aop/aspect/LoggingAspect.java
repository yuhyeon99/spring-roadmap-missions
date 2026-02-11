package com.goorm.springmissionsplayground.mission01_spring_intro.task06_aop.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * mission01_spring_intro 하위의 service 패키지 메서드 실행 시간을 측정한다.
     */
    @Around("execution(* com.goorm.springmissionsplayground.mission01_spring_intro..service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.nanoTime();
            long elapsedMs = (end - start) / 1_000_000;
            log.info("[AOP][{}] executed in {} ms", joinPoint.getSignature().toShortString(), elapsedMs);
        }
    }
}
