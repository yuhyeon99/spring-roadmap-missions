package com.goorm.springmissionsplayground.mission01_spring_intro.task03_di.dto;

import java.util.List;

public class NotificationResponse {
    private final List<String> results;

    public NotificationResponse(List<String> results) {
        this.results = results;
    }

    public List<String> getResults() {
        return results;
    }
}
