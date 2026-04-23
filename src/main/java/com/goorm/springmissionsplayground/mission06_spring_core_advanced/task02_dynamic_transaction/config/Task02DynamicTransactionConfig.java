package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.config;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service.OrderSettlementService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service.OrderSettlementServiceImpl;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction.ConsoleTransactionManager;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction.TransactionInvocationHandler;
import java.lang.reflect.Proxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Task02DynamicTransactionConfig {

    @Bean
    public OrderSettlementService orderSettlementTarget() {
        return new OrderSettlementServiceImpl();
    }

    @Bean
    public OrderSettlementService orderSettlementService(
            OrderSettlementService orderSettlementTarget,
            ConsoleTransactionManager consoleTransactionManager
    ) {
        return (OrderSettlementService) Proxy.newProxyInstance(
                OrderSettlementService.class.getClassLoader(),
                new Class<?>[]{OrderSettlementService.class},
                new TransactionInvocationHandler(orderSettlementTarget, consoleTransactionManager)
        );
    }
}
