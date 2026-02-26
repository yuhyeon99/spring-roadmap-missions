package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectCreateRequest {

    @NotBlank(message = "projectName은 필수입니다.")
    @Size(max = 40, message = "projectName은 40자 이하여야 합니다.")
    private String projectName;

    @NotBlank(message = "owner는 필수입니다.")
    @Size(max = 30, message = "owner는 30자 이하여야 합니다.")
    private String owner;

    @NotBlank(message = "description은 필수입니다.")
    @Size(max = 200, message = "description은 200자 이하여야 합니다.")
    private String description;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
