package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class BaselineAspectMetrics {

    private final AtomicLong invocationCount = new AtomicLong();
    private final AtomicLong snapshotCount = new AtomicLong();
    private final AtomicLong totalObservedNanos = new AtomicLong();
    private final AtomicLong snapshotDigest = new AtomicLong();

    public void recordInvocation(long observedNanos) {
        invocationCount.incrementAndGet();
        totalObservedNanos.addAndGet(observedNanos);
    }

    public void recordSnapshot(String snapshot) {
        snapshotCount.incrementAndGet();
        snapshotDigest.addAndGet(snapshot.hashCode());
    }

    public long invocationCount() {
        return invocationCount.get();
    }

    public long snapshotCount() {
        return snapshotCount.get();
    }

    public long totalObservedNanos() {
        return totalObservedNanos.get();
    }

    public long snapshotDigest() {
        return snapshotDigest.get();
    }

    public void reset() {
        invocationCount.set(0L);
        snapshotCount.set(0L);
        totalObservedNanos.set(0L);
        snapshotDigest.set(0L);
    }
}
