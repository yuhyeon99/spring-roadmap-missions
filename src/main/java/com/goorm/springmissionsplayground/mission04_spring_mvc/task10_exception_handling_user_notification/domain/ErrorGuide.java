package com.goorm.springmissionsplayground.mission04_spring_mvc.task10_exception_handling_user_notification.domain;

public class ErrorGuide {

    private final Long id;
    private final String title;
    private final String summary;
    private final String recommendedAction;

    public ErrorGuide(Long id, String title, String summary, String recommendedAction) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.recommendedAction = recommendedAction;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }
}
