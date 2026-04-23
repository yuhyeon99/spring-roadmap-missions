package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateOperationDispatcher {

    private final Map<String, AbstractOperationTemplate> templates;

    public TemplateOperationDispatcher(List<AbstractOperationTemplate> templateList) {
        this.templates = new LinkedHashMap<>();
        templateList.stream()
                .sorted((left, right) -> left.jobType().compareTo(right.jobType()))
                .forEach(template -> this.templates.put(template.jobType(), template));
    }

    public TemplateJobResult execute(String jobType, String target, String operator) {
        AbstractOperationTemplate template = templates.get(jobType);
        if (template == null) {
            throw new IllegalArgumentException(
                    "지원하지 않는 jobType 입니다. 사용 가능 값: " + String.join(", ", templates.keySet())
            );
        }
        return template.execute(target, operator);
    }

    public List<String> supportedJobTypes() {
        return List.copyOf(templates.keySet());
    }
}
