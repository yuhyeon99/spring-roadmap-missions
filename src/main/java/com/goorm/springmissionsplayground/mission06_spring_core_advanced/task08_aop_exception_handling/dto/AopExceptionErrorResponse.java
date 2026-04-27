package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertEntry;

public class AopExceptionErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final int alertCount;
    private final ExceptionAlertEntry latestAlert;

    public AopExceptionErrorResponse(
            int status,
            String error,
            String message,
            String path,
            int alertCount,
            ExceptionAlertEntry latestAlert
    ) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.alertCount = alertCount;
        this.latestAlert = latestAlert;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public ExceptionAlertEntry getLatestAlert() {
        return latestAlert;
    }
}
