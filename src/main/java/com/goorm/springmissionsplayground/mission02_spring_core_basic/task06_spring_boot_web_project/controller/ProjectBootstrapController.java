package com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectBootstrapResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateRequest;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.dto.ProjectCreateResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task06_spring_boot_web_project.service.ProjectBootstrapService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task06/project-bootstrap")
public class ProjectBootstrapController {

    private final ProjectBootstrapService projectBootstrapService;

    public ProjectBootstrapController(ProjectBootstrapService projectBootstrapService) {
        this.projectBootstrapService = projectBootstrapService;
    }

    @GetMapping
    public ProjectBootstrapResponse summary() {
        return projectBootstrapService.projectSummary();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectCreateResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        return projectBootstrapService.create(request);
    }
}
