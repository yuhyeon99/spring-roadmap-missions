package com.goorm.springmissionsplayground.mission04_spring_mvc.task02_view_resolver.domain;

public class ViewResolverStudyItem {

    private final String stage;
    private final String description;

    public ViewResolverStudyItem(String stage, String description) {
        this.stage = stage;
        this.description = description;
    }

    public String getStage() {
        return stage;
    }

    public String getDescription() {
        return description;
    }
}
