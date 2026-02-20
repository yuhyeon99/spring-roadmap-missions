package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PaymentWorkflowService {

    private final OrderWorkflowService orderWorkflowService;

    public PaymentWorkflowService(@Lazy OrderWorkflowService orderWorkflowService) {
        this.orderWorkflowService = orderWorkflowService;
    }

    public String prepare(String orderId) {
        String orderState = orderWorkflowService.currentOrderState(orderId);
        return "PAYMENT_READY -> " + orderState;
    }
}
