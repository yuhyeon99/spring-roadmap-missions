package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.controller;

import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto.TransactionErrorResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.dto.TransactionSnapshotResponse;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.exception.SettlementFailureException;
import com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction.ConsoleTransactionManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DynamicTransactionController.class)
public class DynamicTransactionExceptionHandler {

    private final ConsoleTransactionManager consoleTransactionManager;

    public DynamicTransactionExceptionHandler(ConsoleTransactionManager consoleTransactionManager) {
        this.consoleTransactionManager = consoleTransactionManager;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SettlementFailureException.class)
    public TransactionErrorResponse handleSettlementFailure(
            SettlementFailureException exception,
            HttpServletRequest request
    ) {
        return new TransactionErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "TX_ROLLBACK",
                exception.getMessage(),
                request.getRequestURI(),
                TransactionSnapshotResponse.from(consoleTransactionManager.getLastTrace())
        );
    }
}
