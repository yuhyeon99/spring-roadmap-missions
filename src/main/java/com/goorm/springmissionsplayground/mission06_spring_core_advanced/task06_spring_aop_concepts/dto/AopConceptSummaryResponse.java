package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task06_spring_aop_concepts.dto;

import java.util.List;

public class AopConceptSummaryResponse {

    private final String missionTopic;
    private final List<String> keywords;
    private final List<String> notes;

    public AopConceptSummaryResponse(String missionTopic, List<String> keywords, List<String> notes) {
        this.missionTopic = missionTopic;
        this.keywords = List.copyOf(keywords);
        this.notes = List.copyOf(notes);
    }

    public String getMissionTopic() {
        return missionTopic;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public List<String> getNotes() {
        return notes;
    }
}
