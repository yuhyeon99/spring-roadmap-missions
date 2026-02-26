package com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.dto.AutoInjectionComparisonResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task04_auto_injection.service.AutoInjectionComparisonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task04/auto-injection")
public class AutoInjectionController {

    private final AutoInjectionComparisonService autoInjectionComparisonService;

    public AutoInjectionController(AutoInjectionComparisonService autoInjectionComparisonService) {
        this.autoInjectionComparisonService = autoInjectionComparisonService;
    }

    @GetMapping
    public AutoInjectionComparisonResponse compare(@RequestParam(defaultValue = "20000") int amount) {
        return autoInjectionComparisonService.compare(amount);
    }
}
