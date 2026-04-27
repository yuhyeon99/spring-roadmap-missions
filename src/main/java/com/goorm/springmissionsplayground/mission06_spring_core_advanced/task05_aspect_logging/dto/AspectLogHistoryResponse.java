package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task05_aspect_logging.support.AspectLogEntry;
import java.util.List;

public class AspectLogHistoryResponse {

    private final int count;
    private final List<AspectLogEntry> entries;

    public AspectLogHistoryResponse(List<AspectLogEntry> entries) {
        this.count = entries.size();
        this.entries = List.copyOf(entries);
    }

    public int getCount() {
        return count;
    }

    public List<AspectLogEntry> getEntries() {
        return entries;
    }
}
