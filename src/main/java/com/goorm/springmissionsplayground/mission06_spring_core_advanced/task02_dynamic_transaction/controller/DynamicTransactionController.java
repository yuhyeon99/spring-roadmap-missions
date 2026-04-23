package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto.SettlementResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto.TransactionSnapshotResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service.OrderSettlementService;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.service.SettlementResult;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction.ConsoleTransactionManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mission06/task02/dynamic-transaction")
public class DynamicTransactionController {

    private final OrderSettlementService orderSettlementService;
    private final ConsoleTransactionManager consoleTransactionManager;

    public DynamicTransactionController(
            OrderSettlementService orderSettlementService,
            ConsoleTransactionManager consoleTransactionManager
    ) {
        this.orderSettlementService = orderSettlementService;
        this.consoleTransactionManager = consoleTransactionManager;
    }

    @PostMapping("/settlements/{orderId}")
    public SettlementResponse settle(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "15000") int amount,
            @RequestParam(defaultValue = "ops-team") String operator
    ) {
        SettlementResult result = orderSettlementService.settle(orderId, amount, operator);
        return toResponse(result);
    }

    @PostMapping("/settlements/{orderId}/fail")
    public SettlementResponse settleWithFailure(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "15000") int amount,
            @RequestParam(defaultValue = "ops-team") String operator
    ) {
        SettlementResult result = orderSettlementService.settleWithFailure(orderId, amount, operator);
        return toResponse(result);
    }

    @GetMapping("/transactions/last-status")
    public TransactionSnapshotResponse lastStatus() {
        return TransactionSnapshotResponse.from(consoleTransactionManager.getLastTrace());
    }

    private SettlementResponse toResponse(SettlementResult result) {
        return new SettlementResponse(
                result.getOrderId(),
                result.getAmount(),
                result.getOperator(),
                result.getBusinessStatus(),
                result.getMessage(),
                TransactionSnapshotResponse.from(consoleTransactionManager.getLastTrace())
        );
    }
}
