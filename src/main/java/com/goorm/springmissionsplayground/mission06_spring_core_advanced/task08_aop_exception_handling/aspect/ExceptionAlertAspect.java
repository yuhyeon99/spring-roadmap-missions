package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.aspect;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.annotation.NotifyOnException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertEntry;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertStore;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionAlertAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionAlertAspect.class);

    private final ExceptionAlertStore exceptionAlertStore;

    public ExceptionAlertAspect(ExceptionAlertStore exceptionAlertStore) {
        this.exceptionAlertStore = exceptionAlertStore;
    }

    @AfterThrowing(
            value = "@annotation(notifyOnException)",
            throwing = "exception"
    )
    public void alertOnFailure(JoinPoint joinPoint, NotifyOnException notifyOnException, Throwable exception) {
        String methodLabel = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();
        String operation = resolveOperationName(notifyOnException, methodLabel);

        ExceptionAlertEntry entry = new ExceptionAlertEntry(
                "AFTER_THROWING",
                operation,
                methodLabel,
                notifyOnException.alertTarget(),
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );

        exceptionAlertStore.add(entry);
        log.error(
                "[TASK08-AOP][ALERT][{}] {} target={} exception={} message={}",
                operation,
                methodLabel,
                entry.getAlertTarget(),
                entry.getExceptionType(),
                entry.getMessage()
        );
    }

    private String resolveOperationName(NotifyOnException notifyOnException, String methodLabel) {
        if (notifyOnException.value() == null || notifyOnException.value().isBlank()) {
            return methodLabel;
        }
        return notifyOnException.value();
    }
}
