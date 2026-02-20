package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto.CircularDependencyResponse;
import org.springframework.stereotype.Service;

@Service
public class OrderWorkflowService {

    private final PaymentWorkflowService paymentWorkflowService;

    public OrderWorkflowService(PaymentWorkflowService paymentWorkflowService) {
        this.paymentWorkflowService = paymentWorkflowService;
    }

    public CircularDependencyResponse process(String orderId) {
        String orderState = currentOrderState(orderId);
        String paymentState = paymentWorkflowService.prepare(orderId);
        return new CircularDependencyResponse(
                orderId,
                orderState,
                paymentState,
                "생성자 주입 + @Lazy 프록시로 순환 의존성을 해소"
        );
    }

    public String currentOrderState(String orderId) {
        return "ORDER_READY(" + orderId + ")";
    }
}
