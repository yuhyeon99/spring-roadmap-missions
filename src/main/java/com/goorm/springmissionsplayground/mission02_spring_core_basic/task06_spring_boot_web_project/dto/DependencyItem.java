package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto;

public class DependencyItem {

    private final String dependency;
    private final String reason;

    public DependencyItem(String dependency, String reason) {
        this.dependency = dependency;
        this.reason = reason;
    }

    public String getDependency() {
        return dependency;
    }

    public String getReason() {
        return reason;
    }
}
