package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

public class ThreadLocalConnectionAuditEntry {

    private final String phase;
    private final String message;

    public ThreadLocalConnectionAuditEntry(String phase, String message) {
        this.phase = phase;
        this.message = message;
    }

    public String getPhase() {
        return phase;
    }

    public String getMessage() {
        return message;
    }
}
