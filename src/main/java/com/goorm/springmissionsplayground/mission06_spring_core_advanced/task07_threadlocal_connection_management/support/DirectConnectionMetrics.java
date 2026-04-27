package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class DirectConnectionMetrics {

    private final AtomicInteger openedConnectionCount = new AtomicInteger();

    public void reset() {
        openedConnectionCount.set(0);
    }

    public void recordOpen() {
        openedConnectionCount.incrementAndGet();
    }

    public int getOpenedConnectionCount() {
        return openedConnectionCount.get();
    }
}
