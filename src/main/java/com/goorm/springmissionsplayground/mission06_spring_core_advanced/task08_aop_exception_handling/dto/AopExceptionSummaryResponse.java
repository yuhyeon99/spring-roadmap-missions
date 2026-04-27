package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task08_aop_exception_handling.dto;

import java.util.List;

public class AopExceptionSummaryResponse {

    private final String summary;
    private final List<String> keywords;
    private final List<String> notes;

    public AopExceptionSummaryResponse(String summary, List<String> keywords, List<String> notes) {
        this.summary = summary;
        this.keywords = List.copyOf(keywords);
        this.notes = List.copyOf(notes);
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<String> getNotes() {
        return notes;
    }
}
