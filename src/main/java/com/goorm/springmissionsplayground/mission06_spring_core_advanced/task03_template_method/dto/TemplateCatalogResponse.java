package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto;

import java.util.List;

public class TemplateCatalogResponse {

    private final String templateMethodDefinition;
    private final List<String> supportedJobTypes;

    public TemplateCatalogResponse(String templateMethodDefinition, List<String> supportedJobTypes) {
        this.templateMethodDefinition = templateMethodDefinition;
        this.supportedJobTypes = List.copyOf(supportedJobTypes);
    }

    public String getTemplateMethodDefinition() {
        return templateMethodDefinition;
    }

    public List<String> getSupportedJobTypes() {
        return supportedJobTypes;
    }
}
