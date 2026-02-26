package com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.dto.ScopeComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task07_singleton_prototype_scope.service.ScopeComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task07/scopes")
public class ScopeComparisonController {

    private final ScopeComparisonService scopeComparisonService;

    public ScopeComparisonController(ScopeComparisonService scopeComparisonService) {
        this.scopeComparisonService = scopeComparisonService;
    }

    @GetMapping
    public ScopeComparisonResponse compare() {
        return scopeComparisonService.compare();
    }
}
