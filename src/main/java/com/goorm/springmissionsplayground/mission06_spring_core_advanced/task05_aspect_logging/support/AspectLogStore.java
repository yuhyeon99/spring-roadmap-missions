package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AspectLogStore {

    private final List<AspectLogEntry> entries = new ArrayList<>();

    public synchronized void reset() {
        entries.clear();
    }

    public synchronized void add(AspectLogEntry entry) {
        entries.add(entry);
    }

    public synchronized List<AspectLogEntry> getEntries() {
        return List.copyOf(entries);
    }
}
