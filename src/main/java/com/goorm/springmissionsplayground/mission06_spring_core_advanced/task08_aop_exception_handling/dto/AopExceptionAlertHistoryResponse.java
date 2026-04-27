package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.support.ExceptionAlertEntry;
import java.util.List;

public class AopExceptionAlertHistoryResponse {

    private final int count;
    private final List<ExceptionAlertEntry> entries;

    public AopExceptionAlertHistoryResponse(List<ExceptionAlertEntry> entries) {
        this.count = entries.size();
        this.entries = List.copyOf(entries);
    }

    public int getCount() {
        return count;
    }

    public List<ExceptionAlertEntry> getEntries() {
        return entries;
    }
}
