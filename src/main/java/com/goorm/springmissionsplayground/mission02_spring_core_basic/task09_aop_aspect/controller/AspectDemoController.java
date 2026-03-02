package com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.dto.AspectDemoResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task09_aop_aspect.service.AspectDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task09/aspect")
public class AspectDemoController {

    private final AspectDemoService aspectDemoService;

    public AspectDemoController(AspectDemoService aspectDemoService) {
        this.aspectDemoService = aspectDemoService;
    }

    @GetMapping("/demo")
    public AspectDemoResponse demo(@RequestParam String topic) {
        String result = aspectDemoService.buildSummary(topic);
        return new AspectDemoResponse(topic, result);
    }
}
