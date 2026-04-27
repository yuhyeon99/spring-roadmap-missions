package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support;

public class AspectLogEntry {

    private final String phase;
    private final String operation;
    private final String method;
    private final String detail;
    private final Long elapsedMs;

    public AspectLogEntry(String phase, String operation, String method, String detail, Long elapsedMs) {
        this.phase = phase;
        this.operation = operation;
        this.method = method;
        this.detail = detail;
        this.elapsedMs = elapsedMs;
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

    public String getDetail() {
        return detail;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }
}
