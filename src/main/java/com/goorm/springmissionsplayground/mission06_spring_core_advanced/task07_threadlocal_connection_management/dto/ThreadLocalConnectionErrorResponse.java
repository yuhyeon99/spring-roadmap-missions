package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

public class ThreadLocalConnectionErrorResponse {

    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ThreadLocalConnectionErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
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
}
