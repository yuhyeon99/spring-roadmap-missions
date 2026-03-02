package com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.dto.SingletonCheckResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task10_singleton_container.service.SingletonContainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task10/singleton")
public class SingletonContainerController {

    private final SingletonContainerService singletonContainerService;

    public SingletonContainerController(SingletonContainerService singletonContainerService) {
        this.singletonContainerService = singletonContainerService;
    }

    @GetMapping("/inspect")
    public SingletonCheckResponse inspect() {
        return singletonContainerService.inspectSingletonBean();
    }
}
