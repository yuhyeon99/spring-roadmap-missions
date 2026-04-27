package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ExceptionAlertStore {

    private final List<ExceptionAlertEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(ExceptionAlertEntry entry) {
        entries.add(entry);
    }

    public synchronized List<ExceptionAlertEntry> getEntries() {
        return List.copyOf(entries);
    }

    public synchronized Optional<ExceptionAlertEntry> getLatestEntry() {
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(entries.size() - 1));
    }
}
