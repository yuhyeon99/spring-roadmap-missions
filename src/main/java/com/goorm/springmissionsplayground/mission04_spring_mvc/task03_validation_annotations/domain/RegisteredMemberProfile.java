package com.goorm.springmissionsplayground.mission04_spring_mvc.task03_validation_annotations.domain;

public class RegisteredMemberProfile {

    private final String name;
    private final String email;
    private final Integer age;
    private final String studyTrackLabel;
    private final String introduction;
    private final String welcomeMessage;
    private final String nextStep;

    public RegisteredMemberProfile(
            String name,
            String email,
            Integer age,
            String studyTrackLabel,
            String introduction,
            String welcomeMessage,
            String nextStep
    ) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.studyTrackLabel = studyTrackLabel;
        this.introduction = introduction;
        this.welcomeMessage = welcomeMessage;
        this.nextStep = nextStep;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    public String getStudyTrackLabel() {
        return studyTrackLabel;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getNextStep() {
        return nextStep;
    }
}
