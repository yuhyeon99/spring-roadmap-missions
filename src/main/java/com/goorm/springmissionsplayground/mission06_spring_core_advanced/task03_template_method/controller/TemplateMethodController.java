package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto.TemplateCatalogResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.dto.TemplateJobResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task03_template_method.service.TemplateOperationDispatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task03/template-method")
public class TemplateMethodController {

    private final TemplateOperationDispatcher templateOperationDispatcher;

    public TemplateMethodController(TemplateOperationDispatcher templateOperationDispatcher) {
        this.templateOperationDispatcher = templateOperationDispatcher;
    }

    @GetMapping("/jobs")
    public TemplateCatalogResponse jobs() {
        return new TemplateCatalogResponse(
                "공통 시작 -> 공통 검증 -> 개별 검증 -> 개별 준비 -> 공통 본 실행 -> 개별 실행 -> 개별 후처리 -> 공통 종료",
                templateOperationDispatcher.supportedJobTypes()
        );
    }

    @PostMapping("/jobs/{jobType}/run")
    public TemplateJobResponse run(
            @PathVariable String jobType,
            @RequestParam String target,
            @RequestParam(defaultValue = "system-operator") String operator
    ) {
        return TemplateJobResponse.from(templateOperationDispatcher.execute(jobType, target, operator));
    }
}
