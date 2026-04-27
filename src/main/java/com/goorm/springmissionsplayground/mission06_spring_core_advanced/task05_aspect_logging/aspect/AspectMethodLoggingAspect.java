package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.annotation.LoggableOperation;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogStore;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectMethodLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AspectMethodLoggingAspect.class);

    private final AspectLogStore aspectLogStore;

    public AspectMethodLoggingAspect(AspectLogStore aspectLogStore) {
        this.aspectLogStore = aspectLogStore;
    }

    @Around("@annotation(loggableOperation)")
    public Object logExecution(ProceedingJoinPoint joinPoint, LoggableOperation loggableOperation) throws Throwable {
        String methodLabel = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();
        String operation = resolveOperationName(loggableOperation, methodLabel);
        String argumentSummary = Arrays.deepToString(joinPoint.getArgs());

        aspectLogStore.reset();
        aspectLogStore.add(new AspectLogEntry("START", operation, methodLabel, "args=" + argumentSummary, null));
        log.info("[TASK05-AOP][START][{}] {} args={}", operation, methodLabel, argumentSummary);

        long start = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            String resultSummary = String.valueOf(result);

            aspectLogStore.add(new AspectLogEntry("END", operation, methodLabel, "result=" + resultSummary, elapsedMs));
            log.info("[TASK05-AOP][END][{}] {} result={} elapsedMs={}", operation, methodLabel, resultSummary, elapsedMs);
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            String errorSummary = throwable.getClass().getSimpleName() + ": " + throwable.getMessage();

            aspectLogStore.add(new AspectLogEntry("ERROR", operation, methodLabel, "error=" + errorSummary, elapsedMs));
            log.info("[TASK05-AOP][ERROR][{}] {} error={} elapsedMs={}", operation, methodLabel, errorSummary, elapsedMs);
            throw throwable;
        }
    }

    private String resolveOperationName(LoggableOperation loggableOperation, String methodLabel) {
        if (loggableOperation.value() == null || loggableOperation.value().isBlank()) {
            return methodLabel;
        }
        return loggableOperation.value();
    }
}
