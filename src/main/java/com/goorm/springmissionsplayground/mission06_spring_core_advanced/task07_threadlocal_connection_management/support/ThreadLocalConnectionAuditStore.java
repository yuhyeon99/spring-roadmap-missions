package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ThreadLocalConnectionAuditStore {

    private final List<ThreadLocalConnectionAuditEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(String phase, String message) {
        entries.add(new ThreadLocalConnectionAuditEntry(phase, message));
    }

    public synchronized List<ThreadLocalConnectionAuditEntry> getEntries() {
        return List.copyOf(entries);
    }
}
