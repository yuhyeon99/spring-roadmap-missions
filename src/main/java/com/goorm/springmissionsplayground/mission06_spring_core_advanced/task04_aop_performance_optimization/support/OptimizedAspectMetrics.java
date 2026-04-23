package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task04_aop_performance_optimization.support;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class OptimizedAspectMetrics {

    private final AtomicLong invocationCount = new AtomicLong();
    private final AtomicLong snapshotCount = new AtomicLong();
    private final AtomicLong totalObservedNanos = new AtomicLong();
    private final AtomicLong snapshotDigest = new AtomicLong();
    private final AtomicLong metadataCacheHitCount = new AtomicLong();
    private final AtomicLong metadataCacheMissCount = new AtomicLong();
    private final ConcurrentHashMap<Method, String> methodLabelCache = new ConcurrentHashMap<>();

    public void recordInvocation(long observedNanos) {
        invocationCount.incrementAndGet();
        totalObservedNanos.addAndGet(observedNanos);
    }

    public void recordSnapshot(String snapshot) {
        snapshotCount.incrementAndGet();
        snapshotDigest.addAndGet(snapshot.hashCode());
    }

    public String resolveMethodLabel(Method method) {
        String cachedLabel = methodLabelCache.get(method);
        if (cachedLabel != null) {
            metadataCacheHitCount.incrementAndGet();
            return cachedLabel;
        }

        metadataCacheMissCount.incrementAndGet();
        String createdLabel = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        methodLabelCache.put(method, createdLabel);
        return createdLabel;
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

    public long metadataCacheHitCount() {
        return metadataCacheHitCount.get();
    }

    public long metadataCacheMissCount() {
        return metadataCacheMissCount.get();
    }

    public void reset() {
        invocationCount.set(0L);
        snapshotCount.set(0L);
        totalObservedNanos.set(0L);
        snapshotDigest.set(0L);
        metadataCacheHitCount.set(0L);
        metadataCacheMissCount.set(0L);
        methodLabelCache.clear();
    }
}
