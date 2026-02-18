package com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.dto.GreetingResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task01_annotation_injection.service.AnnotationGreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task01/greetings")
public class GreetingController {

    private final AnnotationGreetingService greetingService;

    public GreetingController(AnnotationGreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping
    public GreetingResponse greet(@RequestParam(required = false) String name) {
        return greetingService.greet(name);
    }
}
