package com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo;

import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.aop.ExecutionLogStore;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.domain.Order;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.repository.OrderRepository;
import com.goorm.springmissionsplayground.mission02_spring_core_basic.task15_core_principles_demo.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ExecutionLogStore logStore;

    @BeforeEach
    void setUp() {
        orderRepository.clear();
        logStore.clear();
    }

    @Test
    @DisplayName("DI/IoC: 서비스는 인터페이스 타입으로 저장소를 주입받아 주문을 저장한다")
    void placeOrder_savesThroughInjectedRepository() {
        Order saved = orderService.placeOrder("키보드", 50000);

        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("AOP: @LogExecution 메서드 호출 시 실행 로그가 기록된다")
    void aspectLogsExecution() {
        orderService.placeOrder("마우스", 30000);

        assertThat(logStore.getLogs())
            .isNotEmpty()
            .first()
            .satisfies(log -> assertThat(log).contains("placeOrder"));
    }
}
