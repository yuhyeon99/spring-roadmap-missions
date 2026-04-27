package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service;

import java.util.List;

public class ThreadLocalConnectionPerformanceResult {

    private final int workerCount;
    private final int iterationsPerWorker;
    private final long directElapsedMs;
    private final long threadLocalElapsedMs;
    private final int directConnectionAcquisitions;
    private final int threadLocalConnectionAcquisitions;
    private final int reuseSavings;
    private final List<String> notes;

    public ThreadLocalConnectionPerformanceResult(
            int workerCount,
            int iterationsPerWorker,
            long directElapsedMs,
            long threadLocalElapsedMs,
            int directConnectionAcquisitions,
            int threadLocalConnectionAcquisitions,
            int reuseSavings,
            List<String> notes
    ) {
        this.workerCount = workerCount;
        this.iterationsPerWorker = iterationsPerWorker;
        this.directElapsedMs = directElapsedMs;
        this.threadLocalElapsedMs = threadLocalElapsedMs;
        this.directConnectionAcquisitions = directConnectionAcquisitions;
        this.threadLocalConnectionAcquisitions = threadLocalConnectionAcquisitions;
        this.reuseSavings = reuseSavings;
        this.notes = List.copyOf(notes);
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public int getIterationsPerWorker() {
        return iterationsPerWorker;
    }

    public long getDirectElapsedMs() {
        return directElapsedMs;
    }

    public long getThreadLocalElapsedMs() {
        return threadLocalElapsedMs;
    }

    public int getDirectConnectionAcquisitions() {
        return directConnectionAcquisitions;
    }

    public int getThreadLocalConnectionAcquisitions() {
        return threadLocalConnectionAcquisitions;
    }

    public int getReuseSavings() {
        return reuseSavings;
    }

    public List<String> getNotes() {
        return notes;
    }
}
