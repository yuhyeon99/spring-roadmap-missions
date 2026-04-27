package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.support;

public class AopAccessAuditEntry {

    private final String phase;
    private final String message;

    public AopAccessAuditEntry(String phase, String message) {
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
