package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain;

public class StudyTrackOption {

    private final String code;
    private final String label;
    private final String description;

    public StudyTrackOption(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
