package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.service.ThreadLocalConnectionPerformanceResult;
import java.util.List;

public class ThreadLocalConnectionPerformanceResponse {

    private final int workerCount;
    private final int iterationsPerWorker;
    private final long directElapsedMs;
    private final long threadLocalElapsedMs;
    private final int directConnectionAcquisitions;
    private final int threadLocalConnectionAcquisitions;
    private final int reuseSavings;
    private final List<String> notes;

    public ThreadLocalConnectionPerformanceResponse(
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

    public static ThreadLocalConnectionPerformanceResponse from(ThreadLocalConnectionPerformanceResult result) {
        return new ThreadLocalConnectionPerformanceResponse(
                result.getWorkerCount(),
                result.getIterationsPerWorker(),
                result.getDirectElapsedMs(),
                result.getThreadLocalElapsedMs(),
                result.getDirectConnectionAcquisitions(),
                result.getThreadLocalConnectionAcquisitions(),
                result.getReuseSavings(),
                result.getNotes()
        );
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
