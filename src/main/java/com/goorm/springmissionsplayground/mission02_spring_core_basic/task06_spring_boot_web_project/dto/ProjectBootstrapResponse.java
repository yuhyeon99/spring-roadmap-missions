package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

import java.util.List;

public class ProjectBootstrapResponse {

    private final String task;
    private final String basePackage;
    private final List<DependencyItem> dependencies;

    public ProjectBootstrapResponse(
            String task,
            String basePackage,
            List<DependencyItem> dependencies
    ) {
        this.task = task;
        this.basePackage = basePackage;
        this.dependencies = dependencies;
    }

    public String getTask() {
        return task;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public List<DependencyItem> getDependencies() {
        return dependencies;
    }
}
