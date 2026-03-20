package com.goorm.springmissionsplayground.mission04_spring_mvc.task12_model_view_separation.domain;

public class MvcStudySession {

    private final String topic;
    private final String mentor;
    private final String goal;
    private final String referenceViewName;

    public MvcStudySession(String topic, String mentor, String goal, String referenceViewName) {
        this.topic = topic;
        this.mentor = mentor;
        this.goal = goal;
        this.referenceViewName = referenceViewName;
    }

    public String getTopic() {
        return topic;
    }

    public String getMentor() {
        return mentor;
    }

    public String getGoal() {
        return goal;
    }

    public String getReferenceViewName() {
        return referenceViewName;
    }
}
