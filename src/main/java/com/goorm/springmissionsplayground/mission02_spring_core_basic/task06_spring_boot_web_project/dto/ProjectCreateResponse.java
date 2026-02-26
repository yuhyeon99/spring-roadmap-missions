package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

public class ProjectCreateResponse {

    private final String projectName;
    private final String owner;
    private final String message;
    private final String validation;

    public ProjectCreateResponse(String projectName, String owner, String message, String validation) {
        this.projectName = projectName;
        this.owner = owner;
        this.message = message;
        this.validation = validation;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getOwner() {
        return owner;
    }

    public String getMessage() {
        return message;
    }

    public String getValidation() {
        return validation;
    }
}
