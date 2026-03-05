package com.goorm.springmissionsplayground.mission02_spring_core_basic.task13_web_scope.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Scope(value = "application", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ApplicationStats {

    private final AtomicLong totalHits = new AtomicLong(0);

    public long increase() {
        return totalHits.incrementAndGet();
    }

    public long getTotalHits() {
        return totalHits.get();
    }

    public void reset() {
        totalHits.set(0);
    }
}
