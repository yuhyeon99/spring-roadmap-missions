package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

public class ThreadLocalSessionSnapshot {

    private final int connectionId;
    private final String threadName;
    private final int nestingLevel;
    private final String sessionLabel;

    public ThreadLocalSessionSnapshot(int connectionId, String threadName, int nestingLevel, String sessionLabel) {
        this.connectionId = connectionId;
        this.threadName = threadName;
        this.nestingLevel = nestingLevel;
        this.sessionLabel = sessionLabel;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getNestingLevel() {
        return nestingLevel;
    }

    public String getSessionLabel() {
        return sessionLabel;
    }
}
