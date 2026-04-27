package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support;

public class ExceptionAlertEntry {

    private final String phase;
    private final String operation;
    private final String method;
    private final String alertTarget;
    private final String exceptionType;
    private final String message;

    public ExceptionAlertEntry(
            String phase,
            String operation,
            String method,
            String alertTarget,
            String exceptionType,
            String message
    ) {
        this.phase = phase;
        this.operation = operation;
        this.method = method;
        this.alertTarget = alertTarget;
        this.exceptionType = exceptionType;
        this.message = message;
    }

    public String getPhase() {
        return phase;
    }

    public String getOperation() {
        return operation;
    }

    public String getMethod() {
        return method;
    }

    public String getAlertTarget() {
        return alertTarget;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getMessage() {
        return message;
    }
}
