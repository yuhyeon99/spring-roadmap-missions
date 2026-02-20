package com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.dto.CircularDependencyResponse;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task02_circular_dependency.service.OrderWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CircularDependencyResolutionTest {

    @Autowired
    private OrderWorkflowService orderWorkflowService;

    @Test
    void circularDependency_withConstructorInjectionOnly_fails() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(BrokenOrderService.class, BrokenPaymentService.class);

        assertThatThrownBy(context::refresh)
                .isInstanceOf(BeanCreationException.class)
                .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);

        context.close();
    }

    @Test
    void circularDependency_withLazyProxy_isResolved() {
        CircularDependencyResponse response = orderWorkflowService.process("order-2002");

        assertThat(response.getOrderId()).isEqualTo("order-2002");
        assertThat(response.getOrderState()).isEqualTo("ORDER_READY(order-2002)");
        assertThat(response.getPaymentState()).isEqualTo("PAYMENT_READY -> ORDER_READY(order-2002)");
        assertThat(response.getResolution()).isEqualTo("생성자 주입 + @Lazy 프록시로 순환 의존성을 해소");
    }

    @Component
    static class BrokenOrderService {
        BrokenOrderService(BrokenPaymentService paymentService) {
        }
    }

    @Component
    static class BrokenPaymentService {
        BrokenPaymentService(BrokenOrderService orderService) {
        }
    }
}
