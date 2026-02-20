package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.controller;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto.CircularDependencyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service.OrderWorkflowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission02/task02/circular-dependency")
public class CircularDependencyController {

    private final OrderWorkflowService orderWorkflowService;

    public CircularDependencyController(OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    @GetMapping("/resolve")
    public CircularDependencyResponse resolve(@RequestParam(defaultValue = "order-1001") String orderId) {
        return orderWorkflowService.process(orderId);
    }
}
