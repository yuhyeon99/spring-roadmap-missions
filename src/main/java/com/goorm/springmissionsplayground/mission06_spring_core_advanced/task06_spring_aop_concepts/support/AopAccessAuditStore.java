package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AopAccessAuditStore {

    private final List<AopAccessAuditEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(String phase, String message) {
        entries.add(new AopAccessAuditEntry(phase, message));
    }

    public synchronized List<AopAccessAuditEntry> getEntries() {
        return List.copyOf(entries);
    }
}
